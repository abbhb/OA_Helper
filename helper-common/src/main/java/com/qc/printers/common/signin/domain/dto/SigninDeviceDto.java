package com.qc.printers.common.signin.domain.dto;

import lombok.Data;

@Data
public class SigninDeviceDto {
    private String deviceId;
    private String name;


    /**
     * 密钥
     */
    private String secret;

    private String remark;

    // 设备是否在线
    private Boolean online;

    /**
     * 如果设备不在线下面这些都为空
     */

    private String address;

    private int port;

    //支持哪些功能，逗号分割
    private String support;
}
