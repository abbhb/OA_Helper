package com.qc.printers.common.activiti.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;

@Data
public class DeployExt implements Serializable {
    @TableId
    private String deployId;

    private String icon;
}
