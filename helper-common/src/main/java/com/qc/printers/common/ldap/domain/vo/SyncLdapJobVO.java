package com.qc.printers.common.ldap.domain.vo;

import com.qc.printers.common.ldap.domain.entity.SyncLdapJob;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
public class SyncLdapJobVO extends SyncLdapJob implements Serializable {
    private String userName;
}
