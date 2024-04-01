package com.qc.printers.common.signin.domain.entity;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class SigninGroupRule implements Serializable {
    private Long id;

    private Integer rev;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    /**
     * 考勤组详细规则json
     */
    private RulesInfo rulesInfo;
}
