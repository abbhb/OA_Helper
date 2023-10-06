package com.qc.printers.common.print.domain.vo.response;

import lombok.Data;

/**
 * rocketmq消息对象
 *
 * @param <T>
 */
@Data
public class PrintBaseResp<T> {
    private Integer type;
    private T data;
}
