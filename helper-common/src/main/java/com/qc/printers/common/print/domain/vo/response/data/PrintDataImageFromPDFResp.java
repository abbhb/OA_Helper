package com.qc.printers.common.print.domain.vo.response.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 消费者收到 发送给pdf转iamge的数据回复内容数据结构
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PrintDataImageFromPDFResp {
    /**
     * 该id作为uuid
     */
    private String id;//打印存在数据库的id


    /**
     * 预览图下载链接，提前生成好
     */
    private String filePDFImageUrl;

}
