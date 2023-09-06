package com.qc.printers.custom.user.domain.vo.response.role;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class RoleMenuResult implements Serializable {
    private String title;//locale
    private Long key;//menuId
    private Integer sort;
    private List<RoleMenuResult> children;
}
