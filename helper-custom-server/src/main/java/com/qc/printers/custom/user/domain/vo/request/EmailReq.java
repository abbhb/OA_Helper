package com.qc.printers.custom.user.domain.vo.request;

import lombok.Data;

import java.io.Serializable;

@Data
public class EmailReq implements Serializable {
    private String emailCode;

    private String email;
}
