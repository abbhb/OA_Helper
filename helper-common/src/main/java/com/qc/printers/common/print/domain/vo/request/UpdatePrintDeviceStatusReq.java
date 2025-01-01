package com.qc.printers.common.print.domain.vo.request;

import io.swagger.models.auth.In;
import lombok.Data;

import java.io.Serializable;

@Data
public class UpdatePrintDeviceStatusReq implements Serializable {
    private String id;

    private Integer status;
}
