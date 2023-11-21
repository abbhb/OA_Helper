package com.qc.printers.common.notice.domain.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class NoticeAnnex implements Serializable {
    private Long id;

    private Long noticeId;

    private Integer sortNum;

    private String fileUrl;

    private Integer downloadCount;

    @TableField(fill = FieldFill.INSERT)
    @TableLogic//如果加了这个字段就说明这个表里默认都是假删除，mp自带的删除方法都是改状态为1，默认0是不删除。自定义的mybatis得自己写
    private Integer isDeleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT)
    private Long createUser;
}
