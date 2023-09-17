package com.qc.printers.common.contentpromotion.domain;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;

import java.io.Serializable;


@Data
public class DocClassification implements Serializable {
    private Long id;

    /**
     * 分类名
     */
    private String name;

    /**
     * 假删除
     */
    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private Integer isDeleted;
}
