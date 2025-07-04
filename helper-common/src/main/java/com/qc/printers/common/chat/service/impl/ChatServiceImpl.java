package com.qc.printers.common.chat.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.Pair;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qc.printers.common.chat.dao.*;
import com.qc.printers.common.chat.domain.dto.MsgReadInfoDTO;
import com.qc.printers.common.chat.domain.entity.*;
import com.qc.printers.common.chat.domain.enums.*;
import com.qc.printers.common.chat.domain.vo.request.*;
import com.qc.printers.common.chat.domain.vo.response.ChatMemberListResp;
import com.qc.printers.common.chat.domain.vo.response.ChatMemberStatisticResp;
import com.qc.printers.common.chat.domain.vo.response.ChatMessageReadResp;
import com.qc.printers.common.chat.domain.vo.response.ChatMessageResp;
import com.qc.printers.common.chat.service.ChatService;
import com.qc.printers.common.chat.service.ContactService;
import com.qc.printers.common.chat.service.IRoleService;
import com.qc.printers.common.chat.service.adapter.MemberAdapter;
import com.qc.printers.common.chat.service.adapter.MessageAdapter;
import com.qc.printers.common.chat.service.adapter.RoomAdapter;
import com.qc.printers.common.chat.service.cache.RoomCache;
import com.qc.printers.common.chat.service.cache.RoomGroupCache;
import com.qc.printers.common.chat.service.helper.ChatMemberHelper;
import com.qc.printers.common.chat.service.strategy.mark.AbstractMsgMarkStrategy;
import com.qc.printers.common.chat.service.strategy.mark.MsgMarkFactory;
import com.qc.printers.common.chat.service.strategy.msg.AbstractMsgHandler;
import com.qc.printers.common.chat.service.strategy.msg.MsgHandlerFactory;
import com.qc.printers.common.chat.service.strategy.msg.RecallMsgHandler;
import com.qc.printers.common.common.CustomException;
import com.qc.printers.common.common.annotation.RedissonLock;
import com.qc.printers.common.common.domain.enums.NormalOrNoEnum;
import com.qc.printers.common.common.domain.vo.request.CursorPageBaseReq;
import com.qc.printers.common.common.domain.vo.response.CursorPageBaseResp;
import com.qc.printers.common.common.event.MessageSendEvent;
import com.qc.printers.common.common.utils.AssertUtil;
import com.qc.printers.common.config.system.SystemMessageConfig;
import com.qc.printers.common.user.dao.UserDao;
import com.qc.printers.common.user.domain.entity.User;
import com.qc.printers.common.user.domain.enums.ChatActiveStatusEnum;
import com.qc.printers.common.user.domain.enums.RoleEnum;
import com.qc.printers.common.websocket.domain.vo.resp.ws.ChatMemberResp;
import com.qc.printers.common.user.service.cache.UserCache;
import com.qc.printers.transaction.service.MQProducer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nullable;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Description: 消息处理类
 * Author: <a href="https://github.com/zongzibinbin">abin</a>
 * Date: 2023-03-26
 */
@Service
@Slf4j
public class ChatServiceImpl implements ChatService {
    public static final long ROOM_GROUP_ID = 1L;
    @Autowired
    private MessageDao messageDao;
    @Autowired
    private UserDao userDao;
    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;
    @Autowired
    private UserCache userCache;
    @Autowired
    private MemberAdapter memberAdapter;
    @Autowired
    private RoomDao roomDao;
    @Autowired
    private MessageMarkDao messageMarkDao;
    @Autowired
    private RoomFriendDao roomFriendDao;
    @Autowired
    private RecallMsgHandler recallMsgHandler;
    @Autowired
    private ContactService contactService;
    @Autowired
    private ContactDao contactDao;
    @Autowired
    private MessageUserStateDao messageUserStateDao;
    @Autowired
    private RoomCache roomCache;
    @Autowired
    private GroupMemberDao groupMemberDao;
    @Autowired
    private RoomGroupCache roomGroupCache;
    @Autowired
    private MQProducer mqProducer;

    @Autowired
    private RoomGroupDao roomGroupDao;

    @Autowired
    private IRoleService iRoleService;

    @Autowired
    private SystemMessageConfig systemMessageConfig;

    @Autowired
    private SystemMessageDao systemMessageDao;

    @Autowired
    private SystemMessageConfirmDao systemMessageConfirmDao;

    /**
     * 发送消息
     */
    @Override
    @Transactional
    public Long sendMsg(ChatMessageReq request, Long uid) {
        check(request, uid);
        AbstractMsgHandler msgHandler = MsgHandlerFactory.getStrategyNoNull(request.getMsgType());//todo 这里先不扩展，后续再改
        msgHandler.checkMsg(request, uid);
        // 同步获取消息的跳转链接标题
        Message insert = MessageAdapter.buildMsgSave(request, uid);
        messageDao.save(insert);
        msgHandler.saveMsg(insert, request);
        // 如果是系统消息需要往另一张表里存
        checkAndSendSystemMessage(insert,request);
        // 发布消息发送事件
        applicationEventPublisher.publishEvent(new MessageSendEvent(this, insert.getId()));
        return insert.getId();
    }

    /**
     * 如果推送系统消息写入系统消息表，便于顶部系统通知处做聚合查询
     * todo:实不用这个表，直接查全部消息过滤出系统通知房间的消息也行，后期在优化
     * @param insert
     * @param request
     */
    @Transactional
    public void checkAndSendSystemMessage(Message insert, ChatMessageReq request) {
        if (!insert.getFromUid().equals(Long.valueOf(systemMessageConfig.getUserId()))||!insert.getRoomId().equals(Long.valueOf(systemMessageConfig.getRoomId()))) return;
        // 只有房间号和uid都对的上系统消息才算系统消息
        LambdaQueryWrapper<SystemMessage> systemMessageLambdaQueryWrapper = new LambdaQueryWrapper<>();
        systemMessageLambdaQueryWrapper.eq(SystemMessage::getMsgId,insert.getId());
        int count = (int) systemMessageDao.count(systemMessageLambdaQueryWrapper);
        if (count>0)return;// 这条消息已经出现过
        systemMessageDao.save(SystemMessage.builder().msgId(insert.getId()).createUser(insert.getFromUid()).createTime(LocalDateTime.now()).build());
    }

    private void check(ChatMessageReq request, Long uid) {
        Room room = roomCache.get(request.getRoomId());
        // 系统通知最先校验
        if (room.getId().equals(Long.valueOf(systemMessageConfig.getRoomId()))) {
            // 该消息为系统通知，校验是否来自系统通知用户
            if (!uid.equals(Long.valueOf(systemMessageConfig.getUserId()))) {
                throw new CustomException("您不是系统通知用户,请勿伪装成系统发消息");
            }
        }
        if (room.isHotRoom()) {//全员群跳过校验
            return;
        }
        if (room.isRoomFriend()) {
            RoomFriend roomFriend = roomFriendDao.getByRoomId(request.getRoomId());
            AssertUtil.equal(NormalOrNoEnum.NORMAL.getStatus(), roomFriend.getStatus(), "您已经被对方拉黑");
            AssertUtil.isTrue(uid.equals(roomFriend.getUid1()) || uid.equals(roomFriend.getUid2()), "您已经被对方拉黑");
        }
        if (room.isRoomGroup()) {
            RoomGroup roomGroup = roomGroupCache.get(request.getRoomId());
            GroupMember member = groupMemberDao.getMember(roomGroup.getId(), uid);
            AssertUtil.isNotEmpty(member, "您已经被移除该群");
        }

    }

    @Override
    public ChatMessageResp getMsgResp(Message message, Long receiveUid) {
        return CollUtil.getFirst(getMsgRespBatch(Collections.singletonList(message), receiveUid));
    }

    @Override
    public ChatMessageResp getMsgResp(Long msgId, Long receiveUid) {
        Message msg = messageDao.getById(msgId);
        return getMsgResp(msg, receiveUid);
    }

    @Override
    public CursorPageBaseResp<ChatMemberResp> getMemberPage(List<Long> memberUidList, MemberReq request) {
        Pair<ChatActiveStatusEnum, String> pair = ChatMemberHelper.getCursorPair(request.getCursor());
        ChatActiveStatusEnum activeStatusEnum = pair.getKey();
        String timeCursor = pair.getValue();
        List<ChatMemberResp> resultList = new ArrayList<>();//最终列表
        Boolean isLast = Boolean.FALSE;
        if (activeStatusEnum == ChatActiveStatusEnum.ONLINE) {//在线列表
            CursorPageBaseResp<User> cursorPage = userDao.getCursorPage(memberUidList, new CursorPageBaseReq(request.getPageSize(), timeCursor), ChatActiveStatusEnum.ONLINE,request.getSearch());
            resultList.addAll(MemberAdapter.buildMember(cursorPage.getList()));//添加在线列表
            if (cursorPage.getIsLast()) {//如果是最后一页,从离线列表再补点数据
                activeStatusEnum = ChatActiveStatusEnum.OFFLINE;
                Integer leftSize = request.getPageSize() - cursorPage.getList().size();
                cursorPage = userDao.getCursorPage(memberUidList, new CursorPageBaseReq(leftSize, null), ChatActiveStatusEnum.OFFLINE,request.getSearch());
                resultList.addAll(MemberAdapter.buildMember(cursorPage.getList()));//添加离线线列表
            }

            timeCursor = cursorPage.getCursor();
            isLast = cursorPage.getIsLast();
        } else if (activeStatusEnum == ChatActiveStatusEnum.OFFLINE) {//离线列表
            CursorPageBaseResp<User> cursorPage = userDao.getCursorPage(memberUidList, new CursorPageBaseReq(request.getPageSize(), timeCursor), ChatActiveStatusEnum.OFFLINE,request.getSearch());
            resultList.addAll(MemberAdapter.buildMember(cursorPage.getList()));//添加离线线列表
            timeCursor = cursorPage.getCursor();
            isLast = cursorPage.getIsLast();
        }
        // 获取群成员角色ID
        List<Long> uidList = resultList.stream().map(ChatMemberResp::getUid).collect(Collectors.toList());
        RoomGroup roomGroup = roomGroupDao.getByRoomId(request.getRoomId());
        Map<Long, Integer> uidMapRole = groupMemberDao.getMemberMapRole(roomGroup.getId(), uidList);
        resultList.forEach(member -> member.setRoleId(uidMapRole.get(member.getUid())));
        //组装结果
        return new CursorPageBaseResp<>(ChatMemberHelper.generateCursor(activeStatusEnum, timeCursor), isLast, resultList);
    }

    @Override
    public CursorPageBaseResp<ChatMessageResp> getMsgPage(ChatMessagePageReq request, Long receiveUid) {
        // 不显示的消息就直接移除了会话，当有新消息会自动创建会话，或者手动点击发消息

        // 如果是删除消息记录和会话呢？

        //用最后一条消息id，来限制被踢出的人能看见的最大一条消息
        Long lastMsgId = getLastMsgId(request.getRoomId(), receiveUid);
        CursorPageBaseResp<Message> cursorPage = messageDao.getCursorPage(request.getRoomId(), request, lastMsgId,receiveUid);
        if (cursorPage.isEmpty()) {
            return CursorPageBaseResp.empty();
        }
        return CursorPageBaseResp.init(cursorPage, getMsgRespBatch(cursorPage.getList(), receiveUid));
    }

    private Long getLastMsgId(Long roomId, Long receiveUid) {
        Room room = roomCache.get(roomId);
        AssertUtil.isNotEmpty(room, "房间号有误");
        if (room.isHotRoom()) {
            return null;
        }
        AssertUtil.isNotEmpty(receiveUid, "请先登录");
        Contact contact = contactDao.get(receiveUid, roomId);
        return contact.getLastMsgId();
    }

    /**
     * 全员群的在线总人数
     * @return
     */

    @Override
    public ChatMemberStatisticResp getMemberStatistic() {
        System.out.println(Thread.currentThread().getName());
        Long onlineNum = userCache.getOnlineNum();
//        Long offlineNum = userCache.getOfflineNum();不展示总人数
        ChatMemberStatisticResp resp = new ChatMemberStatisticResp();
        resp.setOnlineNum(onlineNum);
//        resp.setTotalNum(onlineNum + offlineNum);
        return resp;
    }

    @Override
    @RedissonLock(key = "#uid")
    public void setMsgMark(Long uid, ChatMessageMarkReq request) {
        AbstractMsgMarkStrategy strategy = MsgMarkFactory.getStrategyNoNull(request.getMarkType());
        switch (MessageMarkActTypeEnum.of(request.getActType())) {
            case MARK:
                strategy.mark(uid, request.getMsgId());
                break;
            case UN_MARK:
                strategy.unMark(uid, request.getMsgId());
                break;
        }
    }

    @Transactional
    @Override
    public void recallMsg(Long uid, ChatMessageBaseReq request) {
        Message message = messageDao.getById(request.getMsgId());
        //校验能不能执行撤回
        checkRecall(uid, message);
        // 如果是系统消息需要撤回系统消息通知
        checkAndRecallSystemMessage(message);
        //执行消息撤回
        recallMsgHandler.recall(uid, message);
    }

    @Transactional
    public void checkAndRecallSystemMessage(Message message) {
        if (!message.getFromUid().equals(Long.valueOf(systemMessageConfig.getUserId()))||!message.getRoomId().equals(Long.valueOf(systemMessageConfig.getRoomId()))) return;
        // 只有房间号和uid都对的上系统消息才算系统消息
        LambdaQueryWrapper<SystemMessage> systemMessageLambdaQueryWrapper = new LambdaQueryWrapper<>();
        systemMessageLambdaQueryWrapper.eq(SystemMessage::getMsgId,message.getId());
        int count = (int) systemMessageDao.count(systemMessageLambdaQueryWrapper);
        if (count==0)return;// 这条消息本身就不存在
        LambdaQueryWrapper<SystemMessage> systemMessageLambdaQueryWrapper1 = new LambdaQueryWrapper<>();
        systemMessageLambdaQueryWrapper1.eq(SystemMessage::getMsgId,message.getId());
        systemMessageDao.remove(systemMessageLambdaQueryWrapper1);

    }

    @Override
    @Cacheable(cacheNames = "member", key = "'memberList.'+#req.roomId")
    public List<ChatMemberListResp> getMemberList(ChatMessageMemberReq req) {
        if (Objects.equals(1L, req.getRoomId())) {//大群聊可看见所有人
            return userDao.getMemberList()
                    .stream()
                    .map(a -> {
                        ChatMemberListResp resp = new ChatMemberListResp();
                        BeanUtils.copyProperties(a, resp);
                        resp.setUid(a.getId());
                        return resp;
                    }).collect(Collectors.toList());
        }
        return null;
    }

    @Override
    public Collection<MsgReadInfoDTO> getMsgReadInfo(Long uid, ChatMessageReadInfoReq request) {
        List<Message> messages = messageDao.listByIds(request.getMsgIds());
        messages.forEach(message -> {
            AssertUtil.equal(uid, message.getFromUid(), "只能查询自己发送的消息");
        });
        return contactService.getMsgReadInfo(messages).values();
    }

    @Override
    public CursorPageBaseResp<ChatMessageReadResp> getReadPage(@Nullable Long uid, ChatMessageReadReq request) {
        Message message = messageDao.getById(request.getMsgId());
        AssertUtil.isNotEmpty(message, "消息id有误");
        AssertUtil.equal(uid, message.getFromUid(), "只能查看自己的消息");
        CursorPageBaseResp<Contact> page;
        if (request.getSearchType() == 1) {//已读
            page = contactDao.getReadPage(message, request);
        } else {
            page = contactDao.getUnReadPage(message, request);
        }
        if (CollectionUtil.isEmpty(page.getList())) {
            return CursorPageBaseResp.empty();
        }
        return CursorPageBaseResp.init(page, RoomAdapter.buildReadResp(page.getList()));
    }

    @Override
    @RedissonLock(key = "#uid")
    public void msgRead(Long uid, ChatMessageMemberReq request) {
        Contact contact = contactDao.get(uid, request.getRoomId());
        if (Objects.nonNull(contact)) {
            Contact update = new Contact();
            update.setId(contact.getId());
            update.setReadTime(new Date());
            contactDao.updateById(update);
        } else {
            Contact insert = new Contact();
            insert.setUid(uid);
            insert.setRoomId(request.getRoomId());
            insert.setReadTime(new Date());
            contactDao.save(insert);
        }
    }

    @Override
    @Transactional
    @RedissonLock(key = "#uid")
    public void setMsgRemove(Long uid, ChatMessageStateReq request) {
        Long msgId = request.getMsgId();
        Message message = messageDao.getById(msgId);
        Long roomId = message.getRoomId();
        LambdaQueryWrapper<MessageUserState> stateLambdaQueryWrapper = new LambdaQueryWrapper<>();
        stateLambdaQueryWrapper.eq(MessageUserState::getRoomId,roomId);
        stateLambdaQueryWrapper.eq(MessageUserState::getMsgId,msgId);

        stateLambdaQueryWrapper.eq(MessageUserState::getUserId,uid);
        MessageUserState one = messageUserStateDao.getOne(stateLambdaQueryWrapper);
        if (Objects.nonNull(one)){
            one.setState(request.getState());
            messageUserStateDao.updateById(one);
        }else {
            one = new MessageUserState();
            one.setRoomId(roomId);
            one.setMsgId(msgId);
            one.setUserId(uid);
            one.setState(request.getState());
            messageUserStateDao.save(one);
        }
    }

    private void checkRecall(Long uid, Message message) {
        AssertUtil.isNotEmpty(message, "消息有误");
        AssertUtil.notEqual(message.getType(), MessageTypeEnum.RECALL.getType(), "消息无法撤回");
        // 超级管理员也不能在单聊行驶特权
        Room room = roomCache.get(message.getRoomId());
        AssertUtil.notEqual(room,null,"不能为空");
        if (room.getHotFlag().equals(HotFlagEnum.YES.getType())){
            // 只有热点群聊拥有此项
            boolean hasPower = iRoleService.hasPower(uid, RoleEnum.CHAT_MANAGER);
            if (hasPower) {
                return;
            }
            throw new CustomException( "抱歉,您没有权限");
        }
        // 群聊的话
        if (room.getType().equals(RoomTypeEnum.GROUP.getType())) {
            RoomGroup roomGroup = roomGroupCache.get(message.getRoomId());
            AssertUtil.notEqual(roomGroup,null,"不能为空");
            GroupMember member = groupMemberDao.getMember(roomGroup.getId(), uid);
            AssertUtil.notEqual(member,null,"群组不存在或者用户不在群组!");
            if (member.getRole().equals(GroupRoleAPPEnum.LEADER.getType())){
                return;
            }
            if (member.getRole().equals(GroupRoleAPPEnum.MANAGER.getType())){
                // 如果是管理员需要判断消息是不是群主和其他管理员的，不是就可以
                GroupMember memberFromUid = groupMemberDao.getMember(roomGroup.getId(), message.getFromUid());
                if (!memberFromUid.getRole().equals(GroupRoleAPPEnum.LEADER.getType())&&memberFromUid.getRole().equals(GroupRoleAPPEnum.MANAGER.getType())){
                    return;
                }else {
                    throw new CustomException( "抱歉,您没有权限");
                }
            }
            throw new CustomException( "抱歉,您没有权限");
        }
        // 单聊
        boolean self = Objects.equals(uid, message.getFromUid());
        AssertUtil.isTrue(self, "抱歉,您没有权限");
        long between = DateUtil.between(Date.from(message.getCreateTime().atZone(ZoneId.systemDefault()).toInstant()), new Date(), DateUnit.MINUTE);
        AssertUtil.isTrue(between < 2, "覆水难收，超过2分钟的消息不能撤回哦~~");
    }

    public List<ChatMessageResp> getMsgRespBatch(List<Message> messages, Long receiveUid) {
        if (CollectionUtil.isEmpty(messages)) {
            return new ArrayList<>();
        }
        Map<Long, Message> replyMap = new HashMap<>();
        //批量查出回复的消息
        List<Long> replyIds = messages.stream().map(Message::getReplyMsgId).filter(Objects::nonNull).distinct().collect(Collectors.toList());
        if (CollectionUtil.isNotEmpty(replyIds)) {
            replyMap = messageDao.listByIds(replyIds).stream().collect(Collectors.toMap(Message::getId, Function.identity()));
        }
        //查询消息标志
        List<MessageMark> msgMark = messageMarkDao.getValidMarkByMsgIdBatch(messages.stream().map(Message::getId).collect(Collectors.toList()));
        return MessageAdapter.buildMsgResp(messages, replyMap, msgMark, receiveUid);
    }

}
