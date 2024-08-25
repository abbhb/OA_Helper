package com.qc.printers.common.chat.domain.vo.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChatRemarkSetReq implements Serializable {

    @NotNull
    @ApiModelProperty("目标id")
    private Long toId;

    @NotNull
    @ApiModelProperty("0为联系人，1为群组")
    private Integer type;

    @ApiModelProperty("备注名")
    private String remarkName;

}
