package com.qc.printers.common.user.domain.entity;

import lombok.Data;

import java.io.Serializable;

@Data
public class SysUserRole implements Serializable {
    private Long userId;

    private Long roleId;
}
