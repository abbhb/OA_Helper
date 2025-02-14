package com.qc.printers.common.print.domain.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class PrintDeviceLinkDto implements Serializable {
    private Long id;

    private String linkId;

    private String linkName;

    private Integer linkType;

    private String printDeviceId;

    private Integer role;

}
