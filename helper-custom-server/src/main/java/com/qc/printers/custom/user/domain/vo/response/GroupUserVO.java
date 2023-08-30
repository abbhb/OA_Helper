package com.qc.printers.custom.user.domain.vo.response;

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
