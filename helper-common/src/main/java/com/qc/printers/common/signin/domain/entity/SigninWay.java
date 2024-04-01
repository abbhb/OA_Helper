package com.qc.printers.common.signin.domain.entity;

import lombok.Data;

import java.io.Serializable;

@Data
public class SigninWay implements Serializable {
    private String type;

    private String deviceId;

}
