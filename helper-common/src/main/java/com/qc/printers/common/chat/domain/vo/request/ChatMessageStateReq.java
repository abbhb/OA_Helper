package com.qc.printers.common.chat.domain.vo.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

/**
 * 消息状态操作请求类
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChatMessageStateReq {
    @NotNull
    @ApiModelProperty("消息id")
    private Long msgId;

    @NotNull
    @ApiModelProperty("标记类型 1删除")
    private Integer state;


}
