package com.qc.printers.custom.print.domain.vo.response;

import lombok.Data;

import java.io.Serializable;

@Data
public class PrinterBaseResp<T> implements Serializable {
    /**
     * 1为完成，取data
     * 0未完成，data为空
     * 2失败，data为message
     */
    Integer type;
    T data;
}
