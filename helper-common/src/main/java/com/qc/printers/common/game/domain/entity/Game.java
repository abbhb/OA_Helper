package com.qc.printers.common.game.domain.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.FastjsonTypeHandler;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@TableName(value = "game", autoResultMap = true)
public class Game implements Serializable {

    private static final long serialVersionUID = 1L;
    private Long id;
    private String gameType;

    // JSON 字段使用 JacksonTypeHandler ，一定要autoResultMap = true
    @TableField(value = "game_data", typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> gameData; // 或其他复杂对象


    private Integer score;

    //这些注解都是调用basemapper才有用,自己写的sql不会生效，插入和更新时都填充
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    private Long userId;
}
