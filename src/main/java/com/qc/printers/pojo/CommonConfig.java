package com.qc.printers.pojo;

import lombok.Data;

import java.io.Serializable;

@Data
public class CommonConfig implements Serializable {

    private String configKey;

    private String configValue;

    //备注
    private String configRemark;
}
