package com.qc.printers.common.chat.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qc.printers.common.chat.dao.*;
import com.qc.printers.common.chat.domain.entity.Room;
import com.qc.printers.common.chat.domain.entity.RoomGroup;
import com.qc.printers.common.chat.domain.vo.request.admin.AdminAddReq;
import com.qc.printers.common.chat.domain.vo.request.admin.AdminRevokeReq;
import com.qc.printers.common.chat.domain.vo.request.member.MemberExitReq;
import com.qc.printers.common.chat.service.IGroupMemberService;
import com.qc.printers.common.chat.service.adapter.MemberAdapter;
import com.qc.printers.common.chat.service.cache.GroupMemberCache;
import com.qc.printers.common.common.exception.CommonErrorEnum;
import com.qc.printers.common.common.utils.AssertUtil;
import com.qc.printers.common.websocket.domain.enums.WSBaseResp;
import com.qc.printers.common.websocket.domain.vo.resp.ws.WSMemberChange;
import com.qc.printers.common.rocketmq.service.impl.PushService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import static com.qc.printers.common.chat.constant.GroupConst.MAX_MANAGE_COUNT;


/**
 * @Author Kkuil
 * @Date 2023/10/24 15:45
 * @Description 群成员服务类
 */
@Service
public class GroupMemberServiceImpl implements IGroupMemberService {

    @Autowired
    private GroupMemberDao groupMemberDao;

    @Autowired
    private RoomGroupDao roomGroupDao;

    @Autowired
    private RoomDao roomDao;

    @Autowired
    private ContactDao contactDao;

    @Autowired
    private MessageDao messageDao;

    @Autowired
    private GroupMemberCache groupMemberCache;

    @Autowired
    private PushService pushService;

    /**
     * 增加管理员
     *
     * @param uid     用户ID
     * @param request 请求信息
     */
    @Override
    public void addAdmin(Long uid, AdminAddReq request) {
        // 1. 判断群聊是否存在
        RoomGroup roomGroup = roomGroupDao.getByRoomId(request.getRoomId());
        AssertUtil.isNotEmpty(roomGroup, "该群不存在~");

        // 2. 判断该用户是否是群主
        Boolean isLord = groupMemberDao.isLord(roomGroup.getId(), uid);
        AssertUtil.isTrue(isLord, "您无权操作~");

        // 3. 判断群成员是否在群中
        Boolean isGroupShip = groupMemberDao.isGroupShip(roomGroup.getRoomId(), request.getUidList());
        AssertUtil.isTrue(isGroupShip, "非法操作，用户不存在群聊中~");

        // 4. 判断管理员数量是否达到上限
        // 4.1 查询现有管理员数量
        List<Long> manageUidList = groupMemberDao.getManageUidList(roomGroup.getId());
        // 4.2 去重
        HashSet<Long> manageUidSet = new HashSet<>(manageUidList);
        manageUidSet.addAll(request.getUidList());
        AssertUtil.isFalse(manageUidSet.size() > MAX_MANAGE_COUNT, "群管理员数量达到上限，请先删除后再操作~");

        // 5. 增加管理员
        groupMemberDao.addAdmin(roomGroup.getId(), request.getUidList());
    }

    /**
     * 撤销管理员
     *
     * @param uid     用户ID
     * @param request 请求信息
     */
    @Override
    public void revokeAdmin(Long uid, AdminRevokeReq request) {
        // 1. 判断群聊是否存在
        RoomGroup roomGroup = roomGroupDao.getByRoomId(request.getRoomId());
        AssertUtil.isNotEmpty(roomGroup, "该群不存在~");

        // 2. 判断该用户是否是群主
        Boolean isLord = groupMemberDao.isLord(roomGroup.getId(), uid);
        AssertUtil.isTrue(isLord, "您无权操作~");

        // 3. 判断群成员是否在群中
        Boolean isGroupShip = groupMemberDao.isGroupShip(roomGroup.getRoomId(), request.getUidList());
        AssertUtil.isTrue(isGroupShip, "非法操作，不允许退出大群聊");

        // 4. 撤销管理员
        groupMemberDao.revokeAdmin(roomGroup.getId(), request.getUidList());
    }

    /**
     * 退出群聊
     *
     * @param uid     需要退出的用户ID
     * @param request 请求信息
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void exitGroup(Long uid, MemberExitReq request) {
        Long roomId = request.getRoomId();
        // 1. 判断群聊是否存在
        RoomGroup roomGroup = roomGroupDao.getByRoomId(roomId);
        AssertUtil.isNotEmpty(roomGroup, "该群不存在~");

        // 2. 判断房间是否是大群聊 （大群聊禁止退出）
        Room room = roomDao.getById(roomId);
        AssertUtil.isFalse(room.isHotRoom(), "非法操作，不允许退出大群聊");

        // 3. 判断群成员是否在群中
        Boolean isGroupShip = groupMemberDao.isGroupShip(roomGroup.getRoomId(), Collections.singletonList(uid));
        AssertUtil.isTrue(isGroupShip, "非法操作，不允许退出大群聊");

        // 4. 判断该用户是否是群主
        Boolean isLord = groupMemberDao.isLord(roomGroup.getId(), uid);
        if (isLord) {
            // 4.1 删除房间
            boolean isDelRoom = roomDao.removeById(roomId);
            AssertUtil.isTrue(isDelRoom, CommonErrorEnum.SYSTEM_ERROR);
            LambdaQueryWrapper<RoomGroup> roomGroupLambdaQueryWrapper = new LambdaQueryWrapper<>();
            roomGroupLambdaQueryWrapper.eq(RoomGroup::getRoomId,roomId);
            boolean remove = roomGroupDao.remove(roomGroupLambdaQueryWrapper);
            AssertUtil.isTrue(remove, CommonErrorEnum.SYSTEM_ERROR);

            // 4.2 删除会话
            Boolean isDelContact = contactDao.removeByRoomId(roomId, Collections.EMPTY_LIST);
            AssertUtil.isTrue(isDelContact, CommonErrorEnum.SYSTEM_ERROR);
            // 4.3 删除群成员
            Boolean isDelGroupMember = groupMemberDao.removeByGroupId(roomGroup.getId(), Collections.EMPTY_LIST);
            AssertUtil.isTrue(isDelGroupMember, CommonErrorEnum.SYSTEM_ERROR);
            // 4.4 删除消息记录 (逻辑删除)
            Boolean isDelMessage = messageDao.removeByRoomId(roomId, Collections.EMPTY_LIST);
            AssertUtil.isTrue(isDelMessage, CommonErrorEnum.SYSTEM_ERROR);
            // TODO 这里也可以告知群成员 群聊已被删除的消息
        } else {
            // 4.5 删除会话
            Boolean isDelContact = contactDao.removeByRoomId(roomId, Collections.singletonList(uid));
            AssertUtil.isTrue(isDelContact, CommonErrorEnum.SYSTEM_ERROR);
            // 4.6 删除群成员
            Boolean isDelGroupMember = groupMemberDao.removeByGroupId(roomGroup.getId(), Collections.singletonList(uid));
            AssertUtil.isTrue(isDelGroupMember, CommonErrorEnum.SYSTEM_ERROR);
            // 4.7 发送移除事件告知群成员
            List<Long> memberUidList = groupMemberCache.getMemberUidList(roomGroup.getRoomId());
            WSBaseResp<WSMemberChange> ws = MemberAdapter.buildMemberRemoveWS(roomGroup.getRoomId(), uid);
            pushService.sendPushMsg(ws, memberUidList);
            groupMemberCache.evictMemberUidList(room.getId());
        }
    }

}
