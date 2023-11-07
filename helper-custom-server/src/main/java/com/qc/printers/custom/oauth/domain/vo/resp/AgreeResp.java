package com.qc.printers.custom.oauth.domain.vo.resp;

import lombok.Data;

import java.io.Serializable;

@Data
public class AgreeResp implements Serializable {
    private String redirectUri;

    private String state;

    private String code;
}
