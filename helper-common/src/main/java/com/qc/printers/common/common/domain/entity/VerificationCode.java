package com.qc.printers.common.common.domain.entity;

import lombok.Data;

import java.io.Serializable;

/**
 * 各类验证码redis类
 * 比如手机验证码，邮箱验证码，注册验证码
 */
@Data
public class VerificationCode implements Serializable {
    private static final long serialVersionUID = 1L;
    private String code;
    //尝试次数,当超过3次就直接抛出异常
    private int tryCount = 0;
}
