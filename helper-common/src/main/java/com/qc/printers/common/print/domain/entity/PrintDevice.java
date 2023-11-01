package com.qc.printers.common.print.domain.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

// 只存在redis
@Data
public class PrintDevice implements Serializable {


    private String printName;

    private String printDescription;
    private String statusTypeMessage;

    private Integer listNums;
    private Integer statusType;


    private List<PrintJobC> printJobs;
}
