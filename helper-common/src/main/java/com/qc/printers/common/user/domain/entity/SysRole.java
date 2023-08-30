package com.qc.printers.common.user.domain.entity;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;


@Data
public class SysRole implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;

    private String name;

    private String key;

    private Integer sort;

    private Integer status;

    private Integer dataScope;

    private Integer isDeleted;

    private Long createUser;

    private LocalDateTime createTime;

    private Long updateUser;

    private LocalDateTime updateTime;
}
