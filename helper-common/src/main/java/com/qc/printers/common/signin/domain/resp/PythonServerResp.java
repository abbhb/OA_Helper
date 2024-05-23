package com.qc.printers.common.signin.domain.resp;

import lombok.Data;

import java.io.Serializable;

@Data
public class PythonServerResp implements Serializable {
    private Integer code;

    private String msg;
}
