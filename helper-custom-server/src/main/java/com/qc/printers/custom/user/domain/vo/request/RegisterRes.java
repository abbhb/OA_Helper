package com.qc.printers.custom.user.domain.vo.request;

import lombok.Data;

import java.io.Serializable;

@Data
public class RegisterRes implements Serializable {

    /**
     * 密码
     */
    private String password;

}
