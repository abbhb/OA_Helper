package com.qc.printers.custom.user.domain.vo.request;


import com.qc.printers.common.user.domain.entity.Group;
import com.qc.printers.common.user.domain.entity.GroupUser;
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
