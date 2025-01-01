package com.qc.printers.common.print.domain.vo.request;

import lombok.Data;

import java.io.Serializable;

@Data
public class PrintDeviceUserQuery implements Serializable {
    private Long printDeviceId;
    private Integer role;
    private Integer pageNum;
    private Integer pageSize;
}
