package com.qc.printers.common.print.domain.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class PrintDeviceUserDto implements Serializable {
    private Long id;

    private String userId;

    private String username;

    private String printDeviceId;

    private Integer role;

}
