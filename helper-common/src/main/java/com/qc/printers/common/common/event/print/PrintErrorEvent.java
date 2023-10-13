package com.qc.printers.common.common.event.print;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class PrintErrorEvent extends ApplicationEvent {
    private Long id;

    public PrintErrorEvent(Object source, Long id) {
        super(source);
        this.id = id;
    }
}
