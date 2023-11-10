package com.qc.printers.custom.user.domain.vo.request;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;

@EqualsAndHashCode(callSuper = true)
@Data
@ToString
public class RegisterEmailRes extends RegisterRes implements Serializable {
    private String emailCode;

    private String email;

}
