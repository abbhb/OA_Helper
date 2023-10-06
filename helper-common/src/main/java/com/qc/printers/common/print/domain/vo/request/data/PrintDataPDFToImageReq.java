package com.qc.printers.common.print.domain.vo.request.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * pdf生成预览图生产设数据类型
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PrintDataPDFToImageReq {
    /**
     * 该id作为uuid
     */
    private String id;//打印存在数据库的id


    /**
     * pdf下载链接
     */
    private String filePDFUrl;


    /**
     * 预览图下载链接，提前生成好
     */
    private String filePDFImageUrl;

    /**
     * 预览图上传链接，提前生成好
     */
    private String filePDFImageUploadUrl;

}
