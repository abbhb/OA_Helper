package com.qc.printers.common.webauthn.vo.req;

import lombok.Data;

import java.io.Serializable;

@Data
public class ActionPasskeyRegistrationReq implements Serializable {
    private String action;

    private String label;

    private String credentialId;


}
