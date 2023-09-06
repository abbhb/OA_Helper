package com.qc.printers.custom.user.domain.vo.response.role;

import com.qc.printers.common.user.domain.entity.SysRole;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;
import java.util.Set;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class RoleManger extends SysRole implements Serializable {
    private Set<Long> haveKey;//已授权哪些菜单
}
