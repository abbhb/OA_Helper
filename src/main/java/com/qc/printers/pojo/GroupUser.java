package com.qc.printers.pojo;

import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;

import java.io.Serializable;

/**
 * Group-User关系表
 */
@Data
public class GroupUser implements Serializable {
    /**
     * 条id
     */
    private Long id;

    /**
     * 组id
     */
    private Long groupId;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 假删除
     */
    @TableLogic
    private Integer isDeleted;
}
