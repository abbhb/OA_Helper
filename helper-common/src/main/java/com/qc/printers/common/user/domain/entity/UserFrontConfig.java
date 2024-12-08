package com.qc.printers.common.user.domain.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor//不加这个是没有有参构造的
@NoArgsConstructor
public class UserFrontConfig implements Serializable {

    @TableField(fill = FieldFill.INSERT)//只在插入时填充
    public LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)//这些注解都是调用basemapper才有用,自己写的sql不会生效，插入和更新时都填充
    public LocalDateTime updateTime;

    @TableId("id")//设置默认主键
    @ApiModelProperty(value = "用户ID")
    public Long id;

    public Long userId;

    private Integer colorWeak;

    private String versionRead;

    private String lastPrintDevice;

    // light dark
    private String theme;

}
