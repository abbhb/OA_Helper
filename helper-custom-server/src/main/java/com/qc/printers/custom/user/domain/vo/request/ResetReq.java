package com.qc.printers.custom.user.domain.vo.request;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
public class ResetReq implements Serializable {
    @NotNull(message = "必须指定用户")
    private Long userId;

}
