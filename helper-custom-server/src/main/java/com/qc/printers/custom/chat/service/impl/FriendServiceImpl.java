package com.qc.printers.custom.chat.service.impl;

import cn.hutool.core.collection.CollectionUtil;

import cn.hutool.extra.pinyin.PinyinUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.google.common.collect.Lists;
import com.qc.printers.common.chat.dao.RoomFriendDao;
import com.qc.printers.common.chat.dao.RoomGroupDao;
import com.qc.printers.common.chat.dao.RoomRemarkDao;
import com.qc.printers.common.chat.domain.dto.GroupRemarkDTO;
import com.qc.printers.common.chat.domain.dto.UserRemarkDTO;
import com.qc.printers.common.chat.domain.entity.RoomFriend;
import com.qc.printers.common.chat.domain.entity.RoomGroup;
import com.qc.printers.common.chat.domain.entity.RoomRemark;
import com.qc.printers.common.chat.domain.vo.response.ChatMemberWListResp;
import com.qc.printers.common.chat.domain.vo.response.ChatMemberWResp;
import com.qc.printers.common.chat.mapper.GroupMemberMapper;
import com.qc.printers.common.chat.service.ChatService;
import com.qc.printers.common.chat.service.ContactService;
import com.qc.printers.common.chat.service.RoomService;
import com.qc.printers.common.chat.service.adapter.MessageAdapter;
import com.qc.printers.common.chatai.properties.ChatGPTProperties;
import com.qc.printers.common.common.annotation.RedissonLock;
import com.qc.printers.common.common.domain.vo.request.CursorPageBaseReq;
import com.qc.printers.common.common.domain.vo.request.PageBaseReq;
import com.qc.printers.common.common.domain.vo.response.CursorPageBaseResp;
import com.qc.printers.common.common.domain.vo.response.PageBaseResp;
import com.qc.printers.common.common.event.UserApplyEvent;
import com.qc.printers.common.common.utils.AssertUtil;
import com.qc.printers.common.common.utils.CursorUtils;
import com.qc.printers.common.common.utils.StringUtils;
import com.qc.printers.common.common.utils.oss.OssDBUtil;
import com.qc.printers.common.user.dao.UserApplyDao;
import com.qc.printers.common.user.dao.UserDao;
import com.qc.printers.common.user.dao.UserFriendDao;
import com.qc.printers.common.user.domain.entity.User;
import com.qc.printers.common.user.domain.entity.UserApply;
import com.qc.printers.common.user.domain.entity.UserFriend;
import com.qc.printers.common.user.domain.vo.request.friend.FriendApplyReq;
import com.qc.printers.common.user.domain.vo.request.friend.FriendApproveReq;
import com.qc.printers.common.user.domain.vo.request.friend.FriendCheckReq;
import com.qc.printers.common.user.domain.vo.response.friend.FriendApplyResp;
import com.qc.printers.common.user.domain.vo.response.friend.FriendCheckResp;
import com.qc.printers.common.user.domain.vo.response.friend.FriendResp;
import com.qc.printers.common.user.domain.vo.response.friend.FriendUnreadResp;
import com.qc.printers.common.user.service.adapter.FriendAdapter;
import com.qc.printers.custom.chat.service.FriendService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.qc.printers.common.user.domain.enums.ApplyStatusEnum.AGREE;
import static com.qc.printers.common.user.domain.enums.ApplyStatusEnum.WAIT_APPROVAL;


/**
 * @author : limeng
 * @description : 好友
 * @date : 2023/07/19
 */
@Slf4j
@Service
public class FriendServiceImpl implements FriendService {

    @Autowired
    private UserFriendDao userFriendDao;

    @Autowired
    private GroupMemberMapper groupMemberMapper;
    @Autowired
    private UserApplyDao userApplyDao;
    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;
    @Autowired
    private RoomService roomService;
    @Autowired
    private ContactService contactService;
    @Autowired
    private ChatService chatService;
    @Autowired
    private UserDao userDao;
    @Autowired
    private RoomRemarkDao roomRemarkDao;
    @Autowired
    private RoomFriendDao roomFriendDao;
    @Autowired
    private RoomGroupDao roomGroupDao;
    @Autowired
    private ChatGPTProperties chatGPTProperties;
    /**
     * 检查
     * 检查是否是自己好友
     *
     * @param uid     uid
     * @param request 请求
     * @return {@link FriendCheckResp}
     */
    @Override
    public FriendCheckResp check(Long uid, FriendCheckReq request) {
        List<UserFriend> friendList = userFriendDao.getByFriends(uid, request.getUidList());

        Set<Long> friendUidSet = friendList.stream().map(UserFriend::getFriendUid).collect(Collectors.toSet());
        List<FriendCheckResp.FriendCheck> friendCheckList = request.getUidList().stream().map(friendUid -> {
            FriendCheckResp.FriendCheck friendCheck = new FriendCheckResp.FriendCheck();
            friendCheck.setUid(friendUid);
            friendCheck.setIsFriend(friendUidSet.contains(friendUid));
            return friendCheck;
        }).collect(Collectors.toList());
        return new FriendCheckResp(friendCheckList);
    }

    /**
     * 申请好友
     *
     * @param request 请求
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @RedissonLock(key = "#uid")
    public void apply(Long uid, FriendApplyReq request) {
        //是否有好友关系
        UserFriend friend = userFriendDao.getByFriend(uid, request.getTargetUid());
        AssertUtil.isEmpty(friend, "你们已经是好友了");
        //是否有待审批的申请记录(自己的)
        UserApply selfApproving = userApplyDao.getFriendApproving(uid, request.getTargetUid());
        if (Objects.nonNull(selfApproving)) {
            log.info("已有好友申请记录,uid:{}, targetId:{}", uid, request.getTargetUid());
            return;
        }
        //是否有待审批的申请记录(别人请求自己的)
        UserApply friendApproving = userApplyDao.getFriendApproving(request.getTargetUid(), uid);
        if (Objects.nonNull(friendApproving)) {
            ((FriendService) AopContext.currentProxy()).applyApprove(uid, new FriendApproveReq(friendApproving.getId()));
            return;
        }
        // 添加chatgpt用户为好友
        if (request.getTargetUid().equals(chatGPTProperties.getAIUserId())){
            //申请入库
            UserApply insert = FriendAdapter.buildFriendApply(uid, request);
            insert.setStatus(AGREE.getCode());
            insert.setType(1);
            userApplyDao.save(insert);
            //创建双方好友关系
            createFriend(uid, request.getTargetUid());
            //创建一个聊天房间
            RoomFriend roomFriend = roomService.createFriendRoom(uid,request.getTargetUid());
            //发送一条同意消息。。我们已经是好友了，开始聊天吧
            chatService.sendMsg(MessageAdapter.buildAgreeMsg(roomFriend.getRoomId()), uid);
            return;
        }
        //申请入库
        UserApply insert = FriendAdapter.buildFriendApply(uid, request);
        userApplyDao.save(insert);
        //申请事件
        applicationEventPublisher.publishEvent(new UserApplyEvent(this, insert));
    }

    /**
     * 分页查询好友申请
     *
     * @param request 请求
     * @return {@link PageBaseResp}<{@link FriendApplyResp}>
     */
    @Override
    public PageBaseResp<FriendApplyResp> pageApplyFriend(Long uid, PageBaseReq request) {
        IPage<UserApply> userApplyIPage = userApplyDao.friendApplyPage(uid, request.plusPage());
        if (CollectionUtil.isEmpty(userApplyIPage.getRecords())) {
            return PageBaseResp.empty();
        }
        //将这些申请列表设为已读
        readApples(uid, userApplyIPage);
        //返回消息
        return PageBaseResp.init(userApplyIPage, FriendAdapter.buildFriendApplyList(userApplyIPage.getRecords()));
    }

    private void readApples(Long uid, IPage<UserApply> userApplyIPage) {
        List<Long> applyIds = userApplyIPage.getRecords()
                .stream().map(UserApply::getId)
                .collect(Collectors.toList());
        userApplyDao.readApples(uid, applyIds);
    }

    /**
     * 申请未读数
     *
     * @return {@link FriendUnreadResp}
     */
    @Override
    public FriendUnreadResp unread(Long uid) {
        Integer unReadCount = userApplyDao.getUnReadCount(uid);
        return new FriendUnreadResp(unReadCount);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @RedissonLock(key = "#uid")
    public void applyApprove(Long uid, FriendApproveReq request) {
        UserApply userApply = userApplyDao.getById(request.getApplyId());
        AssertUtil.isNotEmpty(userApply, "不存在申请记录");
        AssertUtil.equal(userApply.getTargetId(), uid, "不存在申请记录");
        AssertUtil.equal(userApply.getStatus(), WAIT_APPROVAL.getCode(), "已同意好友申请");
        //同意申请
        userApplyDao.agree(request.getApplyId());
        //创建双方好友关系
        createFriend(uid, userApply.getUid());
        //创建一个聊天房间
        RoomFriend roomFriend = roomService.createFriendRoom(uid,userApply.getUid());
        //发送一条同意消息。。我们已经是好友了，开始聊天吧
        chatService.sendMsg(MessageAdapter.buildAgreeMsg(roomFriend.getRoomId()), uid);
    }

    /**
     * 删除好友
     *
     * @param uid       uid
     * @param friendUid 朋友uid
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteFriend(Long uid, Long friendUid) {
        List<UserFriend> userFriends = userFriendDao.getUserFriend(uid, friendUid);
        if (CollectionUtil.isEmpty(userFriends)) {
            log.info("没有好友关系：{},{}", uid, friendUid);
            return;
        }
        List<Long> friendRecordIds = userFriends.stream().map(UserFriend::getId).collect(Collectors.toList());
        userFriendDao.removeByIds(friendRecordIds);
        //禁用房间
        roomService.disableFriendRoom(Arrays.asList(uid, friendUid));
    }

    @Override
    public CursorPageBaseResp<FriendResp> friendList(Long uid, CursorPageBaseReq request) {
        return null;
//        CursorUtils.getCursorPageByMysql(userDao, request,
//                wrapper -> wrapper.orderBy(User::getName), UserFriend::getId);
//
////        CursorPageBaseResp<UserFriend> friendPage = userFriendDao.getFriendPage(uid, request);
//        if (CollectionUtils.isEmpty(friendPage.getList())) {
//            return CursorPageBaseResp.empty();
//        }
//        List<Long> friendUids = friendPage.getList()
//                .stream().map(UserFriend::getFriendUid)
//                .collect(Collectors.toList());
//        List<User> userList = userDao.getFriendList(friendUids);
//        return CursorPageBaseResp.init(friendPage, FriendAdapter.buildFriend(friendPage.getList(), userList));
    }

    /**
     * 暂时只返回联系人，群组之后补充
     * @param userId
     * @return
     */
    // todo:具体对象未构建
    public List<ChatMemberWListResp> getContactList(Long userId) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.ne("id", userId);
        List<ChatMemberWListResp> chatMemberWListResps = new ArrayList<>();
        List<User> users = userDao.list(queryWrapper);

        LambdaQueryWrapper<RoomRemark> roomRemarkLambdaQueryWrapper = new LambdaQueryWrapper<>();
        roomRemarkLambdaQueryWrapper.eq(RoomRemark::getUid,userId);
        /**
         * 我的所有备注，包含的群组和个人
         */
        Map<Long, RoomRemark> roomRemarkMap = roomRemarkDao.list(roomRemarkLambdaQueryWrapper).stream().collect(Collectors.toMap(RoomRemark::getToId, Function.identity()));
        List<UserRemarkDTO> remarkDTOS = users.stream().map(user -> {
            UserRemarkDTO userRemarkDTO = new UserRemarkDTO();
            BeanUtils.copyProperties(user, userRemarkDTO);
            RoomRemark roomRemark = roomRemarkMap.getOrDefault(user.getId(), null);
            if (roomRemark != null && StringUtils.isNotEmpty(roomRemark.getRemarkName())) {
                userRemarkDTO.setRemarkName(roomRemark.getRemarkName());
            }
            return userRemarkDTO;
        }).toList();






        int index = 1;
        chatMemberWListResps.add(ChatMemberWListResp.builder()
                .type(1)
                .key("新的朋友")
                .index(index++)
                .info(List.of(ChatMemberWResp.builder()
                        .name("新的朋友")
                        .type(1)
                        .build()
                ))
                .build()
        );
        // 查询所有的群聊
        List<Long> myAllGroupIds = groupMemberMapper.selectGroupIdsByUid(userId);
        List<RoomGroup> roomGroups = null;
        if (myAllGroupIds!=null&&myAllGroupIds.size()>0){
            roomGroups = roomGroupDao.listByIds(myAllGroupIds);

        }else {
            roomGroups = new ArrayList<>();
        }
        List<GroupRemarkDTO> groupRemarkDTOS = roomGroups.stream().map(roomGroup -> {
            GroupRemarkDTO remarkDTO = new GroupRemarkDTO();
            BeanUtils.copyProperties(roomGroup, remarkDTO);
            RoomRemark roomRemark = roomRemarkMap.getOrDefault(roomGroup.getRoomId(), null);
            if (roomRemark != null && StringUtils.isNotEmpty(roomRemark.getRemarkName())) {
                remarkDTO.setRemarkName(roomRemark.getRemarkName());
            }
            return remarkDTO;
        }).toList();

        List<GroupRemarkDTO> sortedGroupRemarkDTOS = groupRemarkDTOS.stream()
                .sorted(Comparator.comparing(groupRemarkDTO -> {
                    String nameToSort = groupRemarkDTO.getRemarkName() != null ?
                            groupRemarkDTO.getRemarkName() :
                            groupRemarkDTO.getName();
                    String firstLetter = getFirstLetter(nameToSort);
                    return firstLetter.toUpperCase();
                })).toList();



        chatMemberWListResps.add(
                ChatMemberWListResp.builder()
                        .type(2)
                        .key("群聊")
                        .index(index++)
                        .info(sortedGroupRemarkDTOS.stream().map(groupRemarkDTO -> {
                            ChatMemberWResp chatMemberWResp = new ChatMemberWResp();
                            // id就是用户id
                            chatMemberWResp.setId(groupRemarkDTO.getRoomId());
                            chatMemberWResp.setRemarkName(groupRemarkDTO.getRemarkName());
                            chatMemberWResp.setName(groupRemarkDTO.getName());
                            chatMemberWResp.setAvatar(OssDBUtil.toUseUrl(groupRemarkDTO.getAvatar()));
                            chatMemberWResp.setType(2); // Or whatever logic you need for type
                            // 如果有额外的ext字段，可以在这里处理
                            Map<String,Object> objectMap = new HashMap<>();
                            objectMap.put("room_create_time",groupRemarkDTO.getCreateTime());
                            objectMap.put("room_user",groupMemberMapper.selectUidsByGroupId(groupRemarkDTO.getRoomId()));
                            chatMemberWResp.setExt(objectMap);
                            return chatMemberWResp;
                        }).collect(Collectors.toList()))
                        .build()
        );



        // 按字母分组并排序（处理中文字符）
        Map<String, List<UserRemarkDTO>> groupedUsers = remarkDTOS.stream()
                .collect(Collectors.groupingBy(remarkDTO -> {
                    String nameToSort = remarkDTO.getRemarkName() != null ?
                            remarkDTO.getRemarkName() :
                            remarkDTO.getName();
                    String firstLetter = getFirstLetter(nameToSort);
                    return firstLetter.toUpperCase();
                }, TreeMap::new, Collectors.toList()));

        for (Map.Entry<String, List<UserRemarkDTO>> entry : groupedUsers.entrySet()) {
            chatMemberWListResps.add(
                    ChatMemberWListResp.builder()
                    .type(3)
                    .key(entry.getKey())
                    .index(index++)
                    .info(entry.getValue().stream().map(userRemarkDTO -> {
                        ChatMemberWResp chatMemberWResp = new ChatMemberWResp();
                        // id就是用户id
                        chatMemberWResp.setId(userRemarkDTO.getId());
                        chatMemberWResp.setRemarkName(userRemarkDTO.getRemarkName());
                        chatMemberWResp.setName(userRemarkDTO.getName());
                        chatMemberWResp.setAvatar(OssDBUtil.toUseUrl(userRemarkDTO.getAvatar()));
                        chatMemberWResp.setType(3); // Or whatever logic you need for type
                        // 如果有额外的ext字段，可以在这里处理
                        Map<String,Object> objectMap = new HashMap<>();
                        objectMap.put("user_create_time",userRemarkDTO.getCreateTime());
                        objectMap.put("activeStatus",userRemarkDTO.getActiveStatus());
                        chatMemberWResp.setExt(objectMap);
                        return chatMemberWResp;
                    }).collect(Collectors.toList()))
                    .build()
            );
        }

        return chatMemberWListResps;
    }

    private String getFirstLetter(String name) {
        // 使用Hutool的PinyinUtil提取拼音首字母
        return PinyinUtil.getFirstLetter(name, "").substring(0, 1);
    }

    private void createFriend(Long uid, Long targetUid) {

        UserFriend userFriend1 = new UserFriend();
        userFriend1.setUid(uid);
        userFriend1.setFriendUid(targetUid);
        UserFriend userFriend2 = new UserFriend();
        userFriend2.setUid(targetUid);
        userFriend2.setFriendUid(uid);
        userFriendDao.saveBatch(Lists.newArrayList(userFriend1, userFriend2));
    }

}
