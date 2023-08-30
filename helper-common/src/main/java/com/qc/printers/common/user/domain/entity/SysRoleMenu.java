package com.qc.printers.common.user.domain.entity;

import lombok.Data;

import java.io.Serializable;

@Data
public class SysRoleMenu implements Serializable {

    private Long roleId;

    private Long menuId;
}
