package com.qc.printers.custom.user.domain.vo.response;

import lombok.Data;

import java.io.Serializable;

@Data
public class MetaNode implements Serializable {
    private String locale;
    private String icon;
    private Integer order;
}
