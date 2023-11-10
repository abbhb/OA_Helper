package com.qc.printers.custom.user.domain.vo.response;

import lombok.Data;

import java.io.Serializable;

@Data
public class RegisterResp implements Serializable {
    private String token;
}
