package com.qc.printers.common.print.domain.vo.request;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class PrintDeviceLinkReq implements Serializable {
    // 必须是设备主键id，string是为了搭配权限注解
    private String printDeviceId;
    private List<String> linkIds;
    private Integer linkType;
    private Integer role;
}
