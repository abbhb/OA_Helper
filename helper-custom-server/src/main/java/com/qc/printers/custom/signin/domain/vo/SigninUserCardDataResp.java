package com.qc.printers.custom.signin.domain.vo;

import lombok.Data;

import java.io.Serializable;
@Data
public class SigninUserCardDataResp implements Serializable {
    private Long userId;

    private String studentId;

    private String name;

    private String username;

    private String deptName;

    private Long deptId;

    private boolean localExist;

    private boolean deviceExist;
}
