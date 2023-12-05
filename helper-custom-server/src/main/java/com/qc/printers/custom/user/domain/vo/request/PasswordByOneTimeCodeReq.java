package com.qc.printers.custom.user.domain.vo.request;

import lombok.Data;

import java.io.Serializable;

@Data
public class PasswordByOneTimeCodeReq implements Serializable {
    private String password;

    private String rePassword;

    private String oneTimeCode;
}
