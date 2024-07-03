package com.qc.printers.custom.user.domain.vo.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
@AllArgsConstructor
@Data
public class DataScopeResp implements Serializable {
    private Integer type;

    private String name;

    private String desc;
}
