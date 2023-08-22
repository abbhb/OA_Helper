package com.qc.printers.pojo;

import lombok.Data;

import java.io.Serializable;

@Data
public class CommonConfig implements Serializable {

    private String key;

    private String value;

    //备注
    private String remark;
}
