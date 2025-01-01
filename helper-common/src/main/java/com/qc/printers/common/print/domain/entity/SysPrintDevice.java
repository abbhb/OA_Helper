package com.qc.printers.common.print.domain.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;


/**
 * 打印机设备
 */
@Data
public class SysPrintDevice implements Serializable {
    private Long id;

    private String deviceId;

    private String deviceName;

    private String deviceDescription;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT)
    private Long createUser;

    private Integer status;
}
