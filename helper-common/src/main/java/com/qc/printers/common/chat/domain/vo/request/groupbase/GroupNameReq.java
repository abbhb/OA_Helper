package com.qc.printers.common.chat.domain.vo.request.groupbase;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.List;

@Data
public class GroupNameReq implements Serializable {
    @NotNull
    @ApiModelProperty("房间号")
    private Long roomId;

    @NotNull
    @ApiModelProperty("需要改成的name")
    private String name;
}
