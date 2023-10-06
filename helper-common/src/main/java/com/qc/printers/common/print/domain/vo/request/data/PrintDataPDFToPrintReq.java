package com.qc.printers.common.print.domain.vo.request.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * pdf打印的数据类型
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PrintDataPDFToPrintReq {
    /**
     * 该id作为uuid
     */
    private String id;//打印存在数据库的id

    private Integer copies;

    /**
     * 0为单面
     * 1为双面
     */
    private Integer isDuplex;

    /**
     * 打印任务名
     */
    private String name;

    /**
     * 打印起始页
     */
    private Integer startNum;

    /**
     * 打印结束页，最终pdf页数
     */
    private Integer endNum;

    /**
     * pdf下载链接
     */
    private String filePDFUrl;


}
