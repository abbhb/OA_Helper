package com.qc.printers.custom.signin.domain.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class SigninDetailUserInfoResp implements Serializable {
    private String name;
    /**
     * 头像URL
     */
    private String avatar;
    /**
     * 部门
     */
    private String department;

}
