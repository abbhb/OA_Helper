package com.qc.printers.common.user.domain.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor//不加这个是没有有参构造的
@NoArgsConstructor
public class User implements Serializable {
    public static final long serialVersionUID = 1L;

    //value属性用于指定主键的字段
    //type属性用于设置主键生成策略，默认雪花算法


    @TableField(fill = FieldFill.INSERT)//只在插入时填充
    public LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)//这些注解都是调用basemapper才有用,自己写的sql不会生效，插入和更新时都填充
    public LocalDateTime updateTime;

    @TableField(fill = FieldFill.INSERT)
    @TableLogic//如果加了这个字段就说明这个表里默认都是假删除，mp自带的删除方法都是改状态为1，默认0是不删除。自定义的mybatis得自己写
    public Integer isDeleted;

    @TableId("id")//设置默认主键
    @ApiModelProperty(value = "用户ID")
    public Long id;
    @ApiModelProperty(value = "用户名")
    public String username;

    @ApiModelProperty(value = "昵称")
    public String name;

    @ApiModelProperty(value = "手机号")
    public String phone;

    @ApiModelProperty(value = "性别")
    public String sex;

    @ApiModelProperty(value = "学号")
    //学号
    public String studentId;

    @ApiModelProperty(value = "状态")
    public Integer status;

    @ApiModelProperty(value = "部门")
    public Integer deptId;
//    public Long role;//权限更改为角色，再去查询角色所有的权限

    @ApiModelProperty(value = "电子邮箱")
    //绑定邮箱
    public String email;

    @ApiModelProperty(value = "头像")
    public String avatar;

    @ApiModelProperty(value = "密码")
    public String password;

    @ApiModelProperty(value = "盐")
    public String salt;

    @ApiModelProperty(value = "创建用户")
    public Long createUser;

    @ApiModelProperty(value = "最后登录的时间")
    public LocalDateTime loginDate;

    @ApiModelProperty(value = "最后登录的ip")
    public String loginIp;

}
