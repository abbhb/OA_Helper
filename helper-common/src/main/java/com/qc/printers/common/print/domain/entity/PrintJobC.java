package com.qc.printers.common.print.domain.entity;

import lombok.Data;

import java.io.Serializable;

@Data
public class PrintJobC implements Serializable {
    private String id;
    private String documentName;
    private String startTime;
    private String jobStatus;
    private Integer pagesPrinted;
    private Integer pageCount;
}
