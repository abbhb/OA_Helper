package com.qc.printers.common.print.domain.vo.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class UnoServiceInfo implements Serializable {
    /**
     * file to pdf
     * diff
     */
    private Integer toPDFDiffNumber;

    /**
     * pdf to image
     * diff
     */
    private Integer toImageDiffNumber;

    /**
     * file to pdf
     * Consumer
     */
    private Integer toPDFConsumerNumber;

    /**
     * pdf to image
     * Consumer
     */
    private Integer toImageConsumerNumber;

    /**
     * 处理建议
     * ;隔开
     */
    private String chulijianyi;
}
