package com.qc.printers.common.chatai.service;

import com.qc.printers.common.chat.domain.entity.Message;

public interface IChatAIService {

    void chat(Message message);
    void chatForFriendByChatGpt(Message message);
}
