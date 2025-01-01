package com.qc.printers.custom.print.service.strategy.image;

import com.qc.printers.custom.print.domain.dto.PrintImageTypeDto;
import com.qc.printers.common.print.domain.vo.response.PrintImageResp;
import com.qc.printers.common.print.domain.vo.response.PrinterBaseResp;

/**
 * 缩略图返回构造根据状态type不同的策略
 */
public interface ImageTypeRStrategy {
    PrinterBaseResp<PrintImageResp> buildResp(PrintImageTypeDto printImageTypeDto);

}
