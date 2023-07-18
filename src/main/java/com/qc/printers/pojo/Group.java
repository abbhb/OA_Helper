package com.qc.printers.pojo;

import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;

import java.io.Serializable;

@Data
public class Group implements Serializable {
    /**
     * id
     */
    private Long id;

    /**
     * name
     */
    private String name;

    /**
     * 假删除
     */
    @TableLogic
    private Integer isDeleted;
}
