package com.qc.printers.common.chat.domain.vo.response;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class SystemMessageResp implements Serializable {


    private ChatMessageResp message;

    /**
     * 是否已读
     */
    private Boolean read;


    /**
     * 通知的创建时间
     */
    private LocalDateTime createTime;

    private Long createUser;

    private String username;

    private String avatar;
}
