package com.qc.printers.common.chat.consumer;

import com.qc.printers.common.chat.dao.ContactDao;
import com.qc.printers.common.chat.dao.MessageDao;
import com.qc.printers.common.chat.dao.RoomDao;
import com.qc.printers.common.chat.dao.RoomFriendDao;
import com.qc.printers.common.chat.domain.entity.Message;
import com.qc.printers.common.chat.domain.entity.Room;
import com.qc.printers.common.chat.domain.entity.RoomFriend;
import com.qc.printers.common.chat.domain.enums.RoomTypeEnum;
import com.qc.printers.common.chat.domain.vo.response.ChatMessageResp;
import com.qc.printers.common.chat.service.ChatService;
import com.qc.printers.common.chat.service.cache.GroupMemberCache;
import com.qc.printers.common.chat.service.cache.HotRoomCache;
import com.qc.printers.common.chat.service.cache.RoomCache;
import com.qc.printers.common.chatai.service.IChatAIService;
import com.qc.printers.common.common.constant.MQConstant;
import com.qc.printers.common.common.domain.dto.MsgSendMessageDTO;
import com.qc.printers.common.websocket.service.WebSocketService;
import com.qc.printers.common.websocket.service.adapter.WSAdapter;
import com.qc.printers.common.user.service.cache.UserCache;
import com.qc.printers.common.rocketmq.service.impl.PushService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Description: 发送消息更新房间收信箱，并同步给房间成员信箱
 * 这块如果开发环境和线上环境不同会导致消息可能被其他服务消费了而导致开发时发的消息消失
 * Author: <a href="https://github.com/zongzibinbin">abin</a>
 * Date: 2023-08-12
 */
@Slf4j
@RocketMQMessageListener(consumerGroup = MQConstant.SEND_MSG_GROUP, topic = MQConstant.SEND_MSG_TOPIC)
@Component
public class MsgSendConsumer implements RocketMQListener<MsgSendMessageDTO> {
    @Autowired
    private WebSocketService webSocketService;
    @Autowired
    private ChatService chatService;
    @Autowired
    private MessageDao messageDao;
    @Autowired
    private IChatAIService openAIService;
    @Autowired
    private RoomCache roomCache;
    @Autowired
    private RoomDao roomDao;
    @Autowired
    private GroupMemberCache groupMemberCache;
    @Autowired
    private UserCache userCache;
    @Autowired
    private RoomFriendDao roomFriendDao;
    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;
    @Autowired
    private ContactDao contactDao;
    @Autowired
    private HotRoomCache hotRoomCache;
    @Autowired
    private PushService pushService;

    @Override
    public void onMessage(MsgSendMessageDTO dto) {
        Message message = messageDao.getById(dto.getMsgId());
        Room room = roomCache.get(message.getRoomId());
        ChatMessageResp msgResp = chatService.getMsgResp(message, null);
        //所有房间更新房间最新消息
        roomDao.refreshActiveTime(room.getId(), message.getId(), Date.from(message.getCreateTime().atZone(ZoneId.systemDefault()).toInstant()));
        roomCache.delete(room.getId());
        if (room.isHotRoom()) {//热门群聊推送所有在线的人
            //更新热门群聊时间-redis
            hotRoomCache.refreshActiveTime(room.getId(), Date.from(message.getCreateTime().atZone(ZoneId.systemDefault()).toInstant()));
            //推送所有人
            log.info("推送给所有人的消息:{}", WSAdapter.buildMsgSend(msgResp));
            pushService.sendPushMsg(WSAdapter.buildMsgSend(msgResp));
        } else {
            List<Long> memberUidList = new ArrayList<>();
            if (Objects.equals(room.getType(), RoomTypeEnum.GROUP.getType())) {//普通群聊推送所有群成员
                memberUidList = groupMemberCache.getMemberUidList(room.getId());
            } else if (Objects.equals(room.getType(), RoomTypeEnum.FRIEND.getType())) {//单聊对象
                //对单人推送
                RoomFriend roomFriend = roomFriendDao.getByRoomId(room.getId());
//                memberUidList = Arrays.asList(roomFriend.getUid1(), roomFriend.getUid2());
                // 排除自己 message.getFromUid()
                memberUidList = Arrays.asList(roomFriend.getUid1(), roomFriend.getUid2())
                        .stream()
                        .filter(uid -> !uid.equals(message.getFromUid()))
                        .collect(Collectors.toList());

            }
            //更新所有群成员的会话时间
            contactDao.refreshOrCreateActiveTime(room.getId(), memberUidList, message.getId(), Date.from(message.getCreateTime().atZone(ZoneId.systemDefault()).toInstant()));
            //推送房间成员
            pushService.sendPushMsg(WSAdapter.buildMsgSend(msgResp), memberUidList);
        }
    }


}
