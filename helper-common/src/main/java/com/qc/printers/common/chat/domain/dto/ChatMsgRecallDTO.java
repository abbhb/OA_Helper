package com.qc.printers.common.chat.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Description:消息撤回的推送类
 * Author: <a href="https://github.com/zongzibinbin">abin</a>
 * Date: 2023-03-19
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChatMsgRecallDTO {
    private String msgId;
    private String roomId;
    //撤回的用户
    private String recallUid;

    public ChatMsgRecallDTO(Long id, Long roomId, Long recallUid) {
        this.msgId = String.valueOf(id);
        this.roomId = String.valueOf(roomId);
        this.recallUid = String.valueOf(recallUid);
    }
}
