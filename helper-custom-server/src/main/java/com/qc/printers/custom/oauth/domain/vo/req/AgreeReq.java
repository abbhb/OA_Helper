package com.qc.printers.custom.oauth.domain.vo.req;

import lombok.Data;

import java.io.Serializable;

@Data
public class AgreeReq implements Serializable {
    private String responseType;

    private String clientId;

    private String redirectUri;

    private String state;

    private String scope;
}
