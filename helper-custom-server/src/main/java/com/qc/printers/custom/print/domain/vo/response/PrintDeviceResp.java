package com.qc.printers.custom.print.domain.vo.response;

import lombok.Data;

import java.io.Serializable;

@Data
public class PrintDeviceResp implements Serializable {
    //设备id
    private String id;

    //设备名称
    private String name;

    //设备描述
    private String description;

    //设备ip
    private String ip;

    //设备接口端口
    private Integer port;

    //设备服务状态
    private Integer status;
}
