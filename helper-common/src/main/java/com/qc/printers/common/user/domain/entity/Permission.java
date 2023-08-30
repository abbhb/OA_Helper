package com.qc.printers.common.user.domain.entity;

import lombok.Data;

import java.io.Serializable;


@Data
public class Permission implements Serializable {

    private Integer id;

    private String name;

    private Integer weight;
}
