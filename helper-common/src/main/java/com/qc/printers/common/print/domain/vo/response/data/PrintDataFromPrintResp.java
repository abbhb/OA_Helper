package com.qc.printers.common.print.domain.vo.response.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 消费者收到 数据结构
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PrintDataFromPrintResp {
    /**
     * 该id作为uuid
     */
    private String id;//打印存在数据库的id

    /**
     * 1为成功
     * 0为不成功打印
     */
    private Integer isSuccess;
}
