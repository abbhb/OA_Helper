package com.qc.printers.common.chat.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.io.Serializable;

@EqualsAndHashCode(callSuper = false)
@TableName(value = "message_user_state", autoResultMap = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class MessageUserState implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private Long userId;

    private Long roomId;

    private Long msgId;

    @TableField("state")
    private Integer state;
}
