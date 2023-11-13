package com.qc.printers.common.oauth.domain.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class OauthBase implements Serializable {
    /**
     * 当异常时才会返回以下字段
     */
    private Integer code;

    private String msg;
}
