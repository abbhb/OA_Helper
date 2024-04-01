package com.qc.printers.common.signin.domain.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class SigninBc implements Serializable {
    private Long id;

    private String name;

    /**
     * 一天几次上下班
     */
    private Integer everyDay;

    /**
     * 是否为旧数据，1为旧数据
     */
    private Integer bak;

    /**
     * 对象
     */
    private List<BcRule> rules;
}
