package com.qc.printers.custom.print.service.strategy.image;

import com.qc.printers.custom.print.domain.dto.PrintImageTypeDto;
import com.qc.printers.common.print.domain.vo.response.PrintImageResp;
import com.qc.printers.common.print.domain.vo.response.PrinterBaseResp;

/**
 * 开始打印
 */
public class ImageStartPrintType implements ImageTypeRStrategy {
    @Override
    public PrinterBaseResp<PrintImageResp> buildResp(PrintImageTypeDto printImageTypeDto) {
        PrinterBaseResp<PrintImageResp> printImageRespPrinterBaseResp = new PrinterBaseResp<>();
        printImageRespPrinterBaseResp.setType(2);
        printImageRespPrinterBaseResp.setMessage("已经开始打印，无需继续轮询了！");
        return printImageRespPrinterBaseResp;
    }
}
