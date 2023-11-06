package com.qc.printers.common.oauth.domain.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class SysOauthPermission implements Serializable {
    private Long id;

    //为接口地址，比如get_info
    private String key;

    private String intro;

    private Integer isMust;


    @JsonProperty("create_user")
    @TableField(fill = FieldFill.INSERT)
    private Long createUser;

    @TableField(fill = FieldFill.INSERT_UPDATE)//这些注解都是调用basemapper才有用,自己写的sql不会生效，插入和更新时都填充
    private Long updateUser;


    /**
     * 创建时间
     */
    @JsonProperty("create_time")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @JsonProperty("update_time")
    @TableField(fill = FieldFill.INSERT_UPDATE)//这些注解都是调用basemapper才有用,自己写的sql不会生效，插入和更新时都填充
    private LocalDateTime updateTime;

    /**
     * 是否删除
     */
    @JsonProperty("is_deleted")
    @TableField(fill = FieldFill.INSERT)
    @TableLogic
    private Integer isDeleted;

}
