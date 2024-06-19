package com.qc.printers.common.chat.domain.vo.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
public class ContactRemovedReq implements Serializable {
    @NotNull
    @ApiModelProperty("房间id")
    private Long roomId;

}
