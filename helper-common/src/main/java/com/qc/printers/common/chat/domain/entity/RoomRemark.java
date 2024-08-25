package com.qc.printers.common.chat.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = false)
@TableName("room_remark")
public class RoomRemark implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 类型
     * 0：个人，1群组
     */
    @TableField("type")
    private Integer type;

    /**
     * remark_name
     */
    @TableField("remark_name")
    private String remarkName;

    /**
     * uid
     */
    @TableField("uid")
    private Long uid;

    /**
     * 目标id
     * to_id
     */
    @TableField("to_id")
    private Long toId;

}
