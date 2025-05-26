package com.qc.printers.common.chat.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.io.Serializable;
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("system_message_confirm")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SystemMessageConfirm implements Serializable {
    private Long id;

    private Long userId;

    private Long systemMessageId;
    // read为1,阅读并删除为2
    private Integer readType;
}
