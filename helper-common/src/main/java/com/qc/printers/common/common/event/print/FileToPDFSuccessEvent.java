package com.qc.printers.common.common.event.print;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class FileToPDFSuccessEvent extends ApplicationEvent {
    private Long printId;


    public FileToPDFSuccessEvent(Object source, Long printId) {
        super(source);
        this.printId = printId;
    }
}
