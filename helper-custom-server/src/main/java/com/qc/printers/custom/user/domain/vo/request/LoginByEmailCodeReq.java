package com.qc.printers.custom.user.domain.vo.request;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;

@EqualsAndHashCode(callSuper = true)
@Data
@ToString
public class LoginByEmailCodeReq extends EmailReq implements Serializable {

    private Boolean week;

}
