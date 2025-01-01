package com.qc.printers.common.print.domain.vo.response;

import lombok.Data;

import java.io.Serializable;

@Data
public class PrinterBaseResp<T> implements Serializable {
    /**
     * 1为完成，取data
     * 0未完成，data为空
     * 2失败
     */
    Integer type;

    String message;

    T data;
}
