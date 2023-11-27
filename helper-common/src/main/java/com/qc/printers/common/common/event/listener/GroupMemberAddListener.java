package com.qc.printers.common.common.event.listener;

import com.qc.printers.common.chat.dao.GroupMemberDao;
import com.qc.printers.common.chat.domain.entity.GroupMember;
import com.qc.printers.common.chat.domain.entity.RoomGroup;
import com.qc.printers.common.chat.domain.vo.request.ChatMessageReq;
import com.qc.printers.common.chat.service.ChatService;
import com.qc.printers.common.chat.service.adapter.MemberAdapter;
import com.qc.printers.common.chat.service.adapter.RoomAdapter;
import com.qc.printers.common.chat.service.cache.GroupMemberCache;
import com.qc.printers.common.chat.service.cache.MsgCache;
import com.qc.printers.common.common.event.GroupMemberAddEvent;
import com.qc.printers.common.user.dao.UserDao;
import com.qc.printers.common.user.domain.entity.User;
import com.qc.printers.common.user.domain.enums.WSBaseResp;
import com.qc.printers.common.user.domain.vo.response.ws.WSMemberChange;
import com.qc.printers.common.user.service.WebSocketService;
import com.qc.printers.common.user.service.cache.UserCache;
import com.qc.printers.common.user.service.impl.PushService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 添加群成员监听器
 *
 * @author zhongzb create on 2022/08/26
 */
@Slf4j
@Component
public class GroupMemberAddListener {
    @Autowired
    private WebSocketService webSocketService;
    @Autowired
    private ChatService chatService;
    @Autowired
    private MsgCache msgCache;
    @Autowired
    private UserCache userCache;
    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;
    @Autowired
    private GroupMemberDao groupMemberDao;
    @Autowired
    private UserDao userDao;
    @Autowired
    private GroupMemberCache groupMemberCache;
    @Autowired
    private PushService pushService;


    @Async
    @TransactionalEventListener(classes = GroupMemberAddEvent.class, fallbackExecution = true)
    public void sendAddMsg(GroupMemberAddEvent event) {
        List<GroupMember> memberList = event.getMemberList();
        RoomGroup roomGroup = event.getRoomGroup();
        Long inviteUid = event.getInviteUid();
        User user = userCache.getUserInfo(inviteUid);
        Set<Long> uidList = memberList.stream().map(GroupMember::getUid).collect(Collectors.toSet());
        ChatMessageReq chatMessageReq = RoomAdapter.buildGroupAddMessage(roomGroup, user, userCache.getUserInfoBatch(uidList));
        chatService.sendMsg(chatMessageReq, User.UID_SYSTEM);
    }

    @Async
    @TransactionalEventListener(classes = GroupMemberAddEvent.class, fallbackExecution = true)
    public void sendChangePush(GroupMemberAddEvent event) {
        List<GroupMember> memberList = event.getMemberList();
        RoomGroup roomGroup = event.getRoomGroup();
        List<Long> memberUidList = groupMemberCache.getMemberUidList(roomGroup.getRoomId());
        List<Long> uidList = memberList.stream().map(GroupMember::getUid).collect(Collectors.toList());
        List<User> users = userDao.listByIds(uidList);
        users.forEach(user -> {
            WSBaseResp<WSMemberChange> ws = MemberAdapter.buildMemberAddWS(roomGroup.getRoomId(), user);
            pushService.sendPushMsg(ws, memberUidList);
        });
        //移除缓存
        groupMemberCache.evictMemberUidList(roomGroup.getRoomId());
    }

}
