package com.qc.printers.common.print.domain.entity;

import lombok.Data;

import java.io.Serializable;

/**
 * 打印类别统计的数据缓存实体类
 */
@Data
public class PrintDocumentTypeStatistic implements Serializable {
    private String type;

    private Integer count;

    /**
     * 占比
     */
    private Double proportion;
}
