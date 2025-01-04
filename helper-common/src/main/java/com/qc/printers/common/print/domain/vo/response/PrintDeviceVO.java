package com.qc.printers.common.print.domain.vo.response;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class PrintDeviceVO implements Serializable {

    private Long id;

    private String deviceId;
    private String deviceName;
    private String deviceDescription;
    private LocalDateTime createTime;
    private String createUserName;
    private String ownerName;
    private Integer status;
    // 我在该设备的角色
    private Integer userRole;


}
