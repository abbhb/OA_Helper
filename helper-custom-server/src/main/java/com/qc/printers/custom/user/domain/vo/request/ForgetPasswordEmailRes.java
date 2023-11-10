package com.qc.printers.custom.user.domain.vo.request;

import lombok.Data;

import java.io.Serializable;

@Data
public class ForgetPasswordEmailRes implements Serializable {
    private String email;

    private String emailCode;

    private String password;

}
