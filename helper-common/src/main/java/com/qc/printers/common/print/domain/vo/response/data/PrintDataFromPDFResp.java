package com.qc.printers.common.print.domain.vo.response.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 消费者收到 发送给转pdf的数据回复内容数据结构
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PrintDataFromPDFResp {
    /**
     * 该id作为uuid
     */
    private String id;//打印存在数据库的id


    /**
     * pdf下载链接
     */
    private String filePDFUrl;


    /**
     * 下载链接
     */
    private String filePDFImageUrl;

}
