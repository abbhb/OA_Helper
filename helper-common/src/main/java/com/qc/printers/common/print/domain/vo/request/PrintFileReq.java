package com.qc.printers.common.print.domain.vo.request;

import lombok.Data;

import java.io.Serializable;

@Data
public class PrintFileReq implements Serializable {
    //任务id
    private String id;

    //份数
    private Integer copies;

    /**
     * 0为单面
     * 1为双面
     */
    private Integer isDuplex;

    /**
     * 打印起始页
     */
    private Integer startNum;

    /**
     * 打印结束页，最终pdf页数
     */
    private Integer endNum;


    //是否纵向
    private Integer landscape = 0;//默认纵向

    private String deviceId;
}
