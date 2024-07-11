package com.qc.printers.common.user.domain.entity;

import lombok.Data;

import java.io.Serializable;


@Data
public class SysDeptLeaderRole implements Serializable {

    private Long id;

    private Long roleId;

    private Long deptId;
}
