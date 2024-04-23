package com.qc.printers.common.signin.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.FastjsonTypeHandler;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * autoResultMap不手动设置为true就会导致json返回为null
 */
@Data
@TableName(value = "signin_bc", autoResultMap = true)
public class SigninBc implements Serializable {
    @TableId
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
     * 对象,一天几次上下班的上下班时间规则数据
     */
    @TableField(value = "rules", typeHandler = FastjsonTypeHandler.class)
    private List<BcRule> rules;
}
