package com.qc.printers.common.print.domain.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class CancelPrintDto implements Serializable {
    private Integer status;//取消的状态，1为成功，0为失败

    private String msg;//原因
}
