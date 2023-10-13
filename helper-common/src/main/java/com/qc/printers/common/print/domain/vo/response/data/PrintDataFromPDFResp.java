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
     * 文件有多少页
     */
    private Integer pageNums;

    /**
     * 1为成功
     * 0为失败
     */
    private Integer status;

    /**
     * 失败的话原因
     */
    private String message;

}
