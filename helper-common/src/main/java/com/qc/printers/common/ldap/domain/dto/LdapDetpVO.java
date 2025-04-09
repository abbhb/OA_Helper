package com.qc.printers.common.ldap.domain.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class LdapDetpVO implements Serializable {
    private String dn;

    private Long deptId;
}
