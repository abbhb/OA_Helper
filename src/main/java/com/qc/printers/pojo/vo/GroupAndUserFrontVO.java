package com.qc.printers.pojo.vo;

import com.qc.printers.pojo.Group;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class GroupAndUserFrontVO extends Group implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 分组人数
     */
    private Integer count;

    private String createUserName;

    private List<GroupUserVO> groupUserVOList;
}
