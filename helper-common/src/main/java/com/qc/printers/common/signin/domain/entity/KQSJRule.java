package com.qc.printers.common.signin.domain.entity;

import lombok.Data;

import java.io.Serializable;

@Data
public class KQSJRule implements Serializable {
    /**
     * 星期多个逗号分割
     */
    private String xq;

    private Long bcId;
}
