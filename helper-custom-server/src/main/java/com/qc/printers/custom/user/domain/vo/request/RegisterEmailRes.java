package com.qc.printers.custom.user.domain.vo.request;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;

@EqualsAndHashCode(callSuper = true)
@Data
@ToString
public class RegisterEmailRes extends EmailReq implements Serializable {
    /**
     * 密码
     */
    private String password;

}
