package com.qc.printers.custom.oauth.domain.vo.req;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;

@EqualsAndHashCode(callSuper = true)
@Data
@ToString
public class AgreeLoginReq extends AgreeReq implements Serializable {

    private String username;

    private String password;
}
