package com.qc.printers.common.print.domain.dto;

import com.qc.printers.common.print.domain.entity.Printer;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;

/**
 * 从用户上传文件的那一刻就准备好这些
 * 任务开始前就把需要的一些数据缓存到redis
 * 每收到一个id回执都需要更新redis和数据库
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class PrinterRedis extends Printer implements Serializable {
    private boolean isCanGetImage;

    @ApiModelProperty(value = "上传的临时url")
    private String imageUploadUrl;

    @ApiModelProperty(value = "成功后能够下载的url")
    private String imageDownloadUrl;

    @ApiModelProperty(value = "转换后的pdf链接")
    private String pdfUrl;

    //当前状态，缩略图为异步的，不计入
    /**
     * 1为用户刚上传
     * 2为开始转pdf了
     * 3为pdf转完了
     * 4为开始打印了
     * 5打印完成；
     */
    private Integer sTU;

    /**
     * 总页数
     */
    private Integer pageNums;
}