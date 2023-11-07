package com.qc.printers.custom.oauth.domain.vo.resp;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;

@EqualsAndHashCode(callSuper = true)
@Data
@ToString
public class AgreeLoginResp extends AgreeResp implements Serializable {
    private String token;
}
