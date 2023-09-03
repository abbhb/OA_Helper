package com.qc.printers.custom.user.domain.vo.response.menu;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;


@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class MenuResult extends MenuResultNode implements Serializable {

    private List<MenuResultNode> children;
}
