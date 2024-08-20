package com.qc.printers.common.chat.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.FastjsonTypeHandler;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.fasterxml.jackson.databind.JsonNode;
import com.qc.printers.common.chat.domain.entity.msg.MessageExtra;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@TableName(value = "message", autoResultMap = true)// 此处不加json为null
@AllArgsConstructor
@NoArgsConstructor
public class MessageWithStateDto implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 会话表id
     */
    @TableField("room_id")
    private Long roomId;

    /**
     * 消息发送者uid
     */
    @TableField("from_uid")
    private Long fromUid;

    /**
     * 消息内容
     */
    @TableField("content")
    private String content;

    /**
     * 回复的消息内容
     */
    @TableField("reply_msg_id")
    private Long replyMsgId;

    /**
     * 消息状态 0正常 1删除
     *
     * @see com.qc.printers.common.chat.domain.enums.MessageStatusEnum
     */
    @TableField("status")
    private Integer status;

    /**
     * 与回复消息的间隔条数
     */
    @TableField("gap_count")
    private Integer gapCount;

    /**
     * 消息类型 1正常文本 2.撤回消息
     *
     * @see com.qc.printers.common.chat.domain.enums.MessageTypeEnum
     */
    @TableField("type")
    private Integer type;

    /**
     * 消息扩展字段
     * MessageExtra
     */
    @TableField(value = "extra", typeHandler = FastjsonTypeHandler.class)
    private JsonNode extra;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)//这些注解都是调用basemapper才有用,自己写的sql不会生效，插入和更新时都填充
    private LocalDateTime createTime;

    /**
     * 修改时间
     */
    @TableField(value = "update_time")
    private LocalDateTime updateTime;


    @TableField("state")
    private Integer state;


}
