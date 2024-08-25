package com.qc.printers.common.chat.service.impl;

import com.qc.printers.common.chat.dao.GroupMemberDao;
import com.qc.printers.common.chat.dao.RoomDao;
import com.qc.printers.common.chat.dao.RoomFriendDao;
import com.qc.printers.common.chat.dao.RoomGroupDao;
import com.qc.printers.common.chat.domain.entity.GroupMember;
import com.qc.printers.common.chat.domain.entity.Room;
import com.qc.printers.common.chat.domain.entity.RoomFriend;
import com.qc.printers.common.chat.domain.entity.RoomGroup;
import com.qc.printers.common.chat.domain.enums.GroupRoleEnum;
import com.qc.printers.common.chat.domain.enums.RoomTypeEnum;
import com.qc.printers.common.chat.service.RoomService;
import com.qc.printers.common.chat.service.adapter.ChatAdapter;
import com.qc.printers.common.common.annotation.RedissonLock;
import com.qc.printers.common.common.domain.enums.NormalOrNoEnum;
import com.qc.printers.common.common.utils.AssertUtil;
import com.qc.printers.common.user.domain.entity.User;
import com.qc.printers.common.user.service.cache.UserCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Description:
 * Author: <a href="https://github.com/zongzibinbin">abin</a>
 * Date: 2023-07-22
 */
@Service
public class RoomServiceImpl implements RoomService {

    @Autowired
    private RoomFriendDao roomFriendDao;
    @Autowired
    private RoomDao roomDao;
    @Autowired
    private GroupMemberDao groupMemberDao;
    @Autowired
    private UserCache userCache;
    @Autowired
    private RoomGroupDao roomGroupDao;

    @RedissonLock(key = "#uid")
    @Override
    @Transactional(rollbackFor = Exception.class)
    public RoomFriend createFriendRoom(Long uid,Long friendUid) {
        List<Long> uidList = Arrays.asList(uid, friendUid);
        AssertUtil.isNotEmpty(uidList, "房间创建失败，好友数量不对");
        AssertUtil.equal(uidList.size(), 2, "房间创建失败，好友数量不对");
        String key = ChatAdapter.generateRoomKey(uidList);
        RoomFriend roomFriend = roomFriendDao.getByKey(key);
        if (Objects.nonNull(roomFriend)) { //如果存在房间就恢复，适用于恢复好友场景
            restoreRoomIfNeed(roomFriend);
        } else {//新建房间
            Room room = createRoom(RoomTypeEnum.FRIEND);
            roomFriend = createFriendRoom(room.getId(), uidList);
        }
        return roomFriend;
    }

    @Override
    public RoomFriend getFriendRoom(Long uid1, Long uid2) {
        String key = ChatAdapter.generateRoomKey(Arrays.asList(uid1, uid2));
        return roomFriendDao.getByKey(key);
    }

    @Override
    public void disableFriendRoom(List<Long> uidList) {
        AssertUtil.isNotEmpty(uidList, "房间创建失败，好友数量不对");
        AssertUtil.equal(uidList.size(), 2, "房间创建失败，好友数量不对");
        String key = ChatAdapter.generateRoomKey(uidList);
        roomFriendDao.disableRoom(key);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RoomGroup createGroupRoom(Long uid) {
        List<GroupMember> selfGroup = groupMemberDao.getSelfGroup(uid);
        AssertUtil.isEmpty(selfGroup, "每个人只能创建一个群");
        User user = userCache.getUserInfo(uid);
        Room room = createRoom(RoomTypeEnum.GROUP);
        //插入群
        RoomGroup roomGroup = ChatAdapter.buildGroupRoom(user, room.getId());
        roomGroupDao.save(roomGroup);
        //插入群主
        GroupMember leader = GroupMember.builder()
                .role(GroupRoleEnum.LEADER.getType())
                .groupId(roomGroup.getId())
                .uid(uid)
                .build();
        groupMemberDao.save(leader);
        return roomGroup;
    }

    private RoomFriend createFriendRoom(Long roomId, List<Long> uidList) {
        RoomFriend insert = ChatAdapter.buildFriendRoom(roomId, uidList);
        roomFriendDao.save(insert);
        return insert;
    }

    private Room createRoom(RoomTypeEnum typeEnum) {
        Room insert = ChatAdapter.buildRoom(typeEnum);
        roomDao.save(insert);
        return insert;
    }

    private void restoreRoomIfNeed(RoomFriend room) {
        if (Objects.equals(room.getStatus(), NormalOrNoEnum.NOT_NORMAL.getStatus())) {
            roomFriendDao.restoreRoom(room.getId());
        }
    }
}
