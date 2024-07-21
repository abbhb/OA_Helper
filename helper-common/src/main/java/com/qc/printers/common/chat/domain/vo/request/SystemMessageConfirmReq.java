package com.qc.printers.common.chat.domain.vo.request;

import lombok.Data;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.List;

@Data
public class SystemMessageConfirmReq implements Serializable {
    @NotNull
    private List<Long> ids;
}
