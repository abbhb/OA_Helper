package com.qc.printers.common.chat.domain.vo.request.groupbase;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
public class GroupAvatarReq implements Serializable {
    @NotNull
    @ApiModelProperty("房间号")
    private Long roomId;

    @NotNull
    @ApiModelProperty("需要改成的avatar")
    private String avatar;
}
