package com.qc.printers.custom.user.domain.entity;

import lombok.Data;

import java.io.Serializable;

@Data
public class UniquekerLoginUrl implements Serializable {
    private Integer code;

    private String msg;

    private String type;

    private String url;

    /**
     * 登录扫码地址
     * 此地址仅微信和支付宝返回
     */
    private String qrcode;
}
