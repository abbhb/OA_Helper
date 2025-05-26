package com.qc.printers.common.chat.domain.vo.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SystemMessageConfirmReq implements Serializable {
    @NotNull
    private List<Long> ids;
}
