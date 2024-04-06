package com.qc.printers.common.signin.domain.entity;

import lombok.Data;

import java.io.Serializable;

@Data
public class SigninGroup implements Serializable {
    private Long id;

    private String name;

    /**
     * 是否已经被删除 默认0 未删除
     */
    private Integer isRev = 0;
}
