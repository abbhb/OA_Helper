package com.qc.printers.pojo.vo;

import com.qc.printers.pojo.Group;
import com.qc.printers.pojo.GroupUser;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class GroupAndUserVO extends Group implements Serializable {
    /**
     * 已经在JSON转化器中中自动转换Long类型的数据为String
     */

    private List<GroupUser> groupUserList;
}
