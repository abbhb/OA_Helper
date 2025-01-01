package com.qc.printers.common.print.domain.vo.request;

import lombok.Data;

import java.io.Serializable;

@Data
public class CreatePrintDeviceReq implements Serializable {

    private String deviceId;

    private String deviceSecret;

}
