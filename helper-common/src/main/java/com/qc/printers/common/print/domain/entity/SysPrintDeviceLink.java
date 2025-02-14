package com.qc.printers.common.print.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 打印机设备的用户关系
 * user-device 1:N
 * dept-device 1:N 仍各自保证1:N
 * todo:当用户同时处于组授权和用户授权，谁的角色大取谁（role值小）
 */
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class SysPrintDeviceLink implements Serializable {
    private Long id;
    private Long linkId;
    /**
     * 1：user
     * 2：dept
     */
    private Integer linkType;
    private Long printDeviceId;
    /**
     * 1：owner
     * 2: manager
     * 3: user
     */
    private Integer role;

}
