package com.qc.printers.pojo.vo;

import com.qc.printers.pojo.GroupUser;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class GroupUserVO extends GroupUser {
    private static final long serialVersionUID = 1L;

    private String userName;

    //顺带学号
    private String studentId;

    //性别
    private String sex;

}
