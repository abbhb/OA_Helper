package com.qc.printers.common.common.event.print;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class PrintErrorEvent extends ApplicationEvent {
    private Long id;

    private String message;

    public PrintErrorEvent(Object source, Long id, String message) {
        super(source);
        this.id = id;
        this.message = message;
    }
}
