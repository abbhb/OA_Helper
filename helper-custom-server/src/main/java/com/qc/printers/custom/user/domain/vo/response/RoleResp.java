package com.qc.printers.custom.user.domain.vo.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class RoleResp implements Serializable {
    private String id;

    private String name;

    private String key;

    private Integer sort;
}
