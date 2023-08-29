package com.qc.printers.common.common.domain.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class selectOptionsResult implements Serializable {
    private String label;

    private String value;
}
