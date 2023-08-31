package com.qc.printers.custom.user.domain.vo.response;

import lombok.Data;

import java.io.Serializable;

@Data
public class MenuResultNode implements Serializable {
    private String path;
    private String name;
    private MetaNode meta;
}
