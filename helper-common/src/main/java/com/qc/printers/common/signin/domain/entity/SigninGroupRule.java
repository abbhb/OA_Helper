package com.qc.printers.common.signin.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;

@Data
@TableName(value = "signin_group_rule", autoResultMap = true)// 此处不加json为null
public class SigninGroupRule implements Serializable {
    private Long id;

    private Integer rev;

    private LocalDate startTime;

    private LocalDate endTime;

    /**
     * 考勤组详细规则json
     */
    @TableField(value = "rules_info", typeHandler = JacksonTypeHandler.class)
    private RulesInfo rulesInfo;

    private Long groupId;
}
