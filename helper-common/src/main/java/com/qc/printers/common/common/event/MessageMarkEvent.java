package com.qc.printers.common.common.event;

import com.qc.printers.common.chat.domain.dto.ChatMessageMarkDTO;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class MessageMarkEvent extends ApplicationEvent {

    private final ChatMessageMarkDTO dto;

    public MessageMarkEvent(Object source, ChatMessageMarkDTO dto) {
        super(source);
        this.dto = dto;
    }

}
