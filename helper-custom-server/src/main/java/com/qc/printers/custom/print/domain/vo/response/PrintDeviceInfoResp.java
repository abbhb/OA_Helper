package com.qc.printers.custom.print.domain.vo.response;

import com.qc.printers.common.print.domain.entity.PrintJobC;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class PrintDeviceInfoResp implements Serializable {
    private String id;

    private String printName;

    private String printDescription;
    private String statusTypeMessage;

    private Integer listNums;
    private Integer statusType;

    private List<PrintJobC> printJobs;
}
