package com.qc.printers.common.print.domain.dto;

import com.qc.printers.common.print.domain.entity.PrintDevice;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;
import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@ToString
@Data
public class PrintDeviceDto extends PrintDevice implements Serializable {
    private String id;
    //设备ip
    private String ip;

    //设备接口端口
    private Integer port;
    private LocalDateTime lastTime;

}
