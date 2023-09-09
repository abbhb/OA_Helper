package com.qc.printers.custom.user.domain.vo.response.dept;

import com.qc.printers.common.user.domain.entity.SysDept;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class DeptManger extends SysDept implements Serializable {
    private List<DeptManger> children;

}
