package com.qc.printers.custom.user.domain.vo.response.role;

import com.qc.printers.custom.user.domain.vo.response.menu.MenuManger;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class RoleMangerRoot implements Serializable {
    private List<MenuManger> menuMangerList;// 所有的菜单列表

    private List<RoleManger> mangers;

}
