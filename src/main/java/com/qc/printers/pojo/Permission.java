package com.qc.printers.pojo;

import lombok.Data;

import java.io.Serializable;

@Data
public class Permission implements Serializable {

    private Integer id;

    private String name;

    private Integer weight;
}
