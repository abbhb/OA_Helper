package com.qc.printers.custom.print.service.strategy.image;

import com.qc.printers.custom.print.domain.dto.PrintImageTypeDto;
import com.qc.printers.custom.print.domain.vo.response.PrintImageResp;
import com.qc.printers.custom.print.domain.vo.response.PrinterBaseResp;

/**
 * 打印完成的图片处理器类
 */
public class ImagePrintFinishType implements ImageTypeRStrategy {
    @Override
    public PrinterBaseResp<PrintImageResp> buildResp(PrintImageTypeDto printImageTypeDto) {
        PrinterBaseResp<PrintImageResp> printImageRespPrinterBaseResp = new PrinterBaseResp<>();
        printImageRespPrinterBaseResp.setType(2);
        printImageRespPrinterBaseResp.setMessage("已经打印完成，无需继续轮询了！");
        return printImageRespPrinterBaseResp;
    }
}
