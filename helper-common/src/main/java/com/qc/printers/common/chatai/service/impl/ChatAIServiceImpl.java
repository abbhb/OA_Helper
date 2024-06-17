package com.qc.printers.common.chatai.service.impl;

import com.qc.printers.common.chat.domain.entity.Message;
import com.qc.printers.common.chat.domain.entity.msg.MessageExtra;
import com.qc.printers.common.chatai.handler.AbstractChatAIHandler;
import com.qc.printers.common.chatai.handler.ChatAIHandlerFactory;
import com.qc.printers.common.chatai.properties.ChatGPTProperties;
import com.qc.printers.common.chatai.service.IChatAIService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Slf4j
@Service
public class ChatAIServiceImpl implements IChatAIService {
    @Autowired
    private ChatGPTProperties chatGPTProperties;

    /**
     * 群内at GPT
     * @param message
     */
    @Override
    public void chat(Message message) {
        MessageExtra extra = message.getExtra();
        if (extra == null) {
            return;
        }
//        AbstractChatAIHandler chatAI = ChatAIHandlerFactory.getChatAIHandlerByName(message.getContent());
        AbstractChatAIHandler chatAI = ChatAIHandlerFactory.getChatAIHandlerById(extra.getAtUidList().stream().map(Long::valueOf).toList());
        if (chatAI != null) {

            if (CollectionUtils.isEmpty(extra.getAtUidList())) {
                return;
            }
            if (!extra.getAtUidList().contains(String.valueOf(chatGPTProperties.getAIUserId()))) {
                return;
            }
//            chatAI.chat(message);
            chatAI.answerMsg("右键我加我好友再问问题才有更好的体验哦~(群内@询问已经下线)",message);
        }
    }

    @Override
    public void chatForFriendByChatGpt(Message message) {
        MessageExtra extra = message.getExtra();
        if (extra == null) {
            return;
        }
        // 目前仅适配了chatgpt，不支持chatglm
        AbstractChatAIHandler chatAI = ChatAIHandlerFactory.getChatAIHandlerById(chatGPTProperties.getAIUserId());
        if (chatAI != null) {
            chatAI.chatForFriendByChatGpt(message);
        }
    }
}