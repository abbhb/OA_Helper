package com.qc.printers.common.common.event.listener;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qc.printers.common.chat.dao.ContactDao;
import com.qc.printers.common.chat.dao.MessageDao;
import com.qc.printers.common.chat.dao.RoomDao;
import com.qc.printers.common.chat.dao.RoomFriendDao;
import com.qc.printers.common.chat.domain.entity.Message;
import com.qc.printers.common.chat.domain.entity.Room;
import com.qc.printers.common.chat.domain.entity.RoomFriend;
import com.qc.printers.common.chat.domain.enums.HotFlagEnum;
import com.qc.printers.common.chat.service.ChatService;
import com.qc.printers.common.chat.service.cache.GroupMemberCache;
import com.qc.printers.common.chat.service.cache.HotRoomCache;
import com.qc.printers.common.chat.service.cache.RoomCache;
import com.qc.printers.common.chatai.properties.ChatGPTProperties;
import com.qc.printers.common.chatai.service.IChatAIService;
import com.qc.printers.common.common.constant.MQConstant;
import com.qc.printers.common.common.domain.dto.MsgSendMessageDTO;
import com.qc.printers.common.common.event.MessageSendEvent;
import com.qc.printers.common.user.service.WebSocketService;
import com.qc.printers.common.user.service.cache.UserCache;
import com.qc.printers.transaction.service.MQProducer;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.Objects;

/**
 * 消息发送监听器
 *
 * @author zhongzb create on 2022/08/26
 */
@Slf4j
@Component
public class MessageSendListener {
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
    private MQProducer mqProducer;

    @Autowired
    private ChatGPTProperties chatGPTProperties;



    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT, classes = MessageSendEvent.class, fallbackExecution = true)
    public void messageRoute(MessageSendEvent event) {
        Long msgId = event.getMsgId();
        mqProducer.sendSecureMsg(MQConstant.SEND_MSG_TOPIC, new MsgSendMessageDTO(msgId), msgId);
    }

    @TransactionalEventListener(classes = MessageSendEvent.class, fallbackExecution = true)
    public void handlerMsg(@NotNull MessageSendEvent event) {
        Message message = messageDao.getById(event.getMsgId());
        Room room = roomCache.get(message.getRoomId());
        if (isHotRoom(room)) {
            openAIService.chat(message);
        }
        if (isChatUser(room,message.getFromUid())){
            openAIService.chatForFriendByChatGpt(message);
        }
    }

    @Transactional
    public boolean isHotRoom(Room room) {
        return Objects.equals(HotFlagEnum.YES.getType(), room.getHotFlag());
    }
    @Transactional
    public boolean isChatUser(Room room,Long fromUid) {
        LambdaQueryWrapper<RoomFriend> roomFriendLambdaQueryWrapper = new LambdaQueryWrapper<>();
        roomFriendLambdaQueryWrapper.eq(RoomFriend::getRoomId,room.getId());
        roomFriendLambdaQueryWrapper.eq(RoomFriend::getUid1,chatGPTProperties.getAIUserId());
        roomFriendLambdaQueryWrapper.eq(RoomFriend::getUid2,fromUid);
        int count = roomFriendDao.count(roomFriendLambdaQueryWrapper);

        if (count!=0){
            return true;
        }
        return false;
    }

//    @TransactionalEventListener(classes = MessageSendEvent.class, fallbackExecution = true)
//    public void publishChatToWechat(@NotNull MessageSendEvent event) {
//        Message message = messageDao.getById(event.getMsgId());
//        if (Objects.nonNull(message.getExtra().getAtUidList())) {
//            weChatMsgOperationService.publishChatMsgToWeChatUser(message.getFromUid(), message.getExtra().getAtUidList(),
//                    message.getContent());
//        }
//    }
}
