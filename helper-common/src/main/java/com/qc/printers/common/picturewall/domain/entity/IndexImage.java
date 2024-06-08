package com.qc.printers.common.picturewall.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.FastjsonTypeHandler;
import com.qc.printers.common.notice.domain.entity.Notice;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
@TableName(value = "index_image", autoResultMap = true)
public class IndexImage implements Serializable {
    private Long id;

    private String label;

    @TableField(value = "data", typeHandler = FastjsonTypeHandler.class)
    private List<IndexImageData> data;

    private Integer sort;

    /**
     * 1：全体部门可见
     * 2：查可见部门表，在里面的可见
     */
    private Integer visibility;

}
