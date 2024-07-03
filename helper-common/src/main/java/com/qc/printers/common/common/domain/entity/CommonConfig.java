package com.qc.printers.common.common.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;

@Data
public class CommonConfig implements Serializable {
    @TableId
    private String configKey;

    private String configValue;

    //备注
    private String configRemark;
}
