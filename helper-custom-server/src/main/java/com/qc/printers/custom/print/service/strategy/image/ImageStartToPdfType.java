package com.qc.printers.custom.print.service.strategy.image;

import com.qc.printers.custom.print.domain.dto.PrintImageTypeDto;
import com.qc.printers.common.print.domain.vo.response.PrintImageResp;
import com.qc.printers.common.print.domain.vo.response.PrinterBaseResp;

/**
 * 开始转pdf
 */
public class ImageStartToPdfType implements ImageTypeRStrategy {
    @Override
    public PrinterBaseResp<PrintImageResp> buildResp(PrintImageTypeDto printImageTypeDto) {
        PrinterBaseResp<PrintImageResp> printImageRespPrinterBaseResp = new PrinterBaseResp<>();
        printImageRespPrinterBaseResp.setType(0);
        return printImageRespPrinterBaseResp;
    }
}
