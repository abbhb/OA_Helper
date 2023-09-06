package com.qc.printers.common.user.domain.dto;

import com.qc.printers.common.user.domain.entity.SysRole;
import com.qc.printers.common.user.domain.entity.SysRoleMenu;
import lombok.*;

import java.io.Serializable;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
@AllArgsConstructor//不加这个是没有有参构造的
@NoArgsConstructor
public class RoleMenuDTO extends SysRole implements Serializable {
    private List<SysRoleMenu> sysRoleMenus;
}
