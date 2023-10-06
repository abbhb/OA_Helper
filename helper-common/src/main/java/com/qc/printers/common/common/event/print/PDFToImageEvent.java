package com.qc.printers.common.common.event.print;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class PDFToImageEvent extends ApplicationEvent {
    private Long printId;


    public PDFToImageEvent(Object source, Long printId) {
        super(source);
        this.printId = printId;
    }
}
