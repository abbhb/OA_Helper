package com.qc.printers.common.common.event.print;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class PDFToImageSuccessEvent extends ApplicationEvent {
    private Long printId;


    public PDFToImageSuccessEvent(Object source, Long printId) {
        super(source);
        this.printId = printId;
    }
}
