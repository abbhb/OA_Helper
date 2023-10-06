package com.qc.printers.common.print.domain.vo.request.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * v1版本 生产者发送给转pdf的数据内容
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PrintDataFileToPDFReq {
    /**
     * 该id作为uuid
     */
    private String id;//打印存在数据库的id

    /**
     * 源文件下载链接
     */
    private String fileUrl;

    /**
     * pdf下载链接
     */
    private String filePDFUrl;

    /**
     * pdf OSS上传链接
     */
    private String filePDFUploadUrl;


}
