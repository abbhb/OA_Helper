package com.qc.printers.common.print.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 打印机设备的用户关系
 */
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class SysPrintDeviceUser implements Serializable {
    private Long id;
    private Long userId;
    private Long printDeviceId;
    /**
     * 1：owner
     * 2: manager
     * 3: user
     */
    private Integer role;

}
