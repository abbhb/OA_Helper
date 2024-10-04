
package com.qc.printers.common.print.domain.dto;

import lombok.Data;

import java.io.Serializable;


@Data
public class DiffItem implements Serializable {
    /**
     * chat_send_msg
     */
    private String topic;
    /**
     * chat_send_msg_group
     */
    private String group;

    /**
     * 在线的消费者数
     */
    private Integer countOfOnlineConsumers;

    /**
     * 该消费者堆积数
     */
    private Integer diff;


}
