package com.qc.printers.common.signin.domain.entity;

import lombok.Data;

import java.io.Serializable;

@Data
public class SigninWay implements Serializable {
    /**
     * 逗号分割
     */
    private String type;

    private String deviceId;

}
