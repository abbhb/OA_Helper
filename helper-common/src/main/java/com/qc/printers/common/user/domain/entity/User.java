package com.qc.printers.common.user.domain.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.qc.printers.common.common.annotation.Excel;
import com.qc.printers.common.ldap.utils.PasswordRsaUtil;
import com.qc.printers.common.websocket.domain.entity.IpInfo;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.time.LocalDateTime;

@Slf4j
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User implements Serializable {
    public static final long serialVersionUID = 1L;

    /**
     * 指定系统（chatgpt）用户的UID
     */
    public static final long UID_SYSTEM = 1659939726386827265L;
//    public static final long UID_SYSTEM = 1659939726386827265L;

    //value属性用于指定主键的字段
    //type属性用于设置主键生成策略，默认雪花算法


    public User(LocalDateTime createTime, LocalDateTime updateTime, Integer isDeleted, Long id, String username, String name, String phone, String sex, String studentId, Integer status, Long deptId, String email, String avatar, String password, String salt,String rsaPassword, Long createUser, LocalDateTime loginDate, IpInfo loginIp, Integer activeStatus) {
        this.createTime = createTime;
        this.updateTime = updateTime;
        this.isDeleted = isDeleted;
        this.id = id;
        this.username = username;
        this.name = name;
        this.phone = phone;
        this.sex = sex;
        this.studentId = studentId;
        this.status = status;
        this.deptId = deptId;
        this.email = email;
        this.avatar = avatar;
        this.password = password;
        this.salt = salt;
        this.rsaPassword = rsaPassword;
        this.createUser = createUser;
        this.loginDate = loginDate;
        this.loginIp = loginIp;
        this.activeStatus = activeStatus;
    }

    @TableField(fill = FieldFill.INSERT)//只在插入时填充
    public LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)//这些注解都是调用basemapper才有用,自己写的sql不会生效，插入和更新时都填充
    public LocalDateTime updateTime;

    @TableField(fill = FieldFill.INSERT)
    @TableLogic//如果加了这个字段就说明这个表里默认都是假删除，mp自带的删除方法都是改状态为1，默认0是不删除。自定义的mybatis得自己写
    public Integer isDeleted;

    @Excel(name = "用户编号", width = 30, cellType = Excel.ColumnType.STRING, prompt = "用户编号",type = Excel.Type.EXPORT)
    @TableId("id")//设置默认主键
    @ApiModelProperty(value = "用户ID")
    public Long id;

    @Excel(name = "用户名", width = 30, cellType = Excel.ColumnType.STRING, prompt = "用户名",type = Excel.Type.EXPORT)
    @ApiModelProperty(value = "用户名")
    public String username;


    @Excel(name = "昵称", width = 30, cellType = Excel.ColumnType.STRING, prompt = "昵称")
    @ApiModelProperty(value = "昵称")
    public String name;

    @ApiModelProperty(value = "手机号")
    public String phone;

    @ApiModelProperty(value = "性别")
    public String sex;

    @Excel(name = "学号", width = 30, cellType = Excel.ColumnType.STRING, prompt = "学号")
    @ApiModelProperty(value = "学号")
    //学号
    public String studentId;

    @ApiModelProperty(value = "状态")
    public Integer status;

    @Excel(name = "部门编号", width = 30, cellType = Excel.ColumnType.STRING, prompt = "部门编号")
    @ApiModelProperty(value = "部门")
    public Long deptId;
//    public Long role;//权限更改为角色，再去查询角色所有的权限

    @Excel(name = "电子邮箱[可做用户名]", width = 30, cellType = Excel.ColumnType.STRING, prompt = "电子邮箱[可做用户名]")
    @ApiModelProperty(value = "电子邮箱")
    //绑定邮箱
    public String email;

    @ApiModelProperty(value = "头像")
    public String avatar;

    @Excel(name = "密码[建议8位以上]", width = 30, cellType = Excel.ColumnType.STRING, prompt = "密码[建议8位以上]",type = Excel.Type.IMPORT)
    @ApiModelProperty(value = "密码")
    public String password;

    @ApiModelProperty(value = "盐")
    public String salt;

    @ApiModelProperty(value = "真实密码")
    public String rsaPassword;

    @ApiModelProperty(value = "创建用户")
    public Long createUser;

    @ApiModelProperty(value = "最后登录的时间")
    public LocalDateTime loginDate;

    @ApiModelProperty(value = "最后登录的ip")
    @TableField(value = "login_ip", typeHandler = JacksonTypeHandler.class)
    public IpInfo loginIp;

    public Integer activeStatus;

    /**
     * 用于拼接数据权限等额外sql
     */
    @TableField(exist = false)
    private String existSql;


    public String getJieMiPassword() {
        // 解密逻辑: 解密成功返回解密后的密码, 解密失败返回一个登陆不上的密码并打log
        try {
            return PasswordRsaUtil.decrypt(this.rsaPassword);
        } catch (Exception e) {
            log.error("密码解密失败,请检查密码是否正确,用户名:{}", this.username);
            return "SuijiFuZaMiMa"+System.currentTimeMillis();
        }
    }

}
