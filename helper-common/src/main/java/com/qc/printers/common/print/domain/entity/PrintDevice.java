package com.qc.printers.common.print.domain.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 打印机设备--原始注册数据
 * 只存在redis,用于用户轮询设备状态
 */
@Data
public class PrintDevice implements Serializable {


    private String printName;

    private String printDescription;
    private String statusTypeMessage;

    private Integer listNums;
    private Integer statusType;


    private List<PrintJobC> printJobs;
}
