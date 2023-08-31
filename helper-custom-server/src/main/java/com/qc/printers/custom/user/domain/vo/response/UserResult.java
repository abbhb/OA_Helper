package com.qc.printers.custom.user.domain.vo.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserResult implements Serializable {

    private String id;

    private String username;

    private String name;

    private String phone;

    private String sex;

    private String studentId;

    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    //分组
    private String deptId;
    //权限名
    private String deptName;

    private String email;

    private String avatar;

    private List<String> roles;

}
