package com.qc.printers.common.signin.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serializable;

@AllArgsConstructor
@Getter
public enum RuleEnum implements Serializable {
    GUDING(1, "固定时间上下班"),
    ;

    private final Integer code;

    private final String desc;
}


