package com.qc.printers.common.print.domain.vo.request;

import lombok.Data;

/**
 * rocketmq消息对象
 *
 * @param <T>
 */
@Data
public class PrintBaseReq<T> {
    private Integer type;
    private T data;

    public PrintBaseReq(Integer type, T data) {
        this.type = type;
        this.data = data;
    }
}
