package com.qc.printers.common.print.domain.vo.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;


@ToString(callSuper = true)
@Data
@AllArgsConstructor//不加这个是没有有参构造的
@NoArgsConstructor
public class PrintDeviceResp implements Serializable {
    //设备id
    private String id;

    //设备名称
    private String name;

    //设备描述
    private String description;


    //设备服务状态
    private Integer status;
}
