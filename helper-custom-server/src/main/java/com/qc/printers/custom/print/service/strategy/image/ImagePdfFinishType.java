package com.qc.printers.custom.print.service.strategy.image;

import com.qc.printers.custom.print.domain.dto.PrintImageTypeDto;
import com.qc.printers.common.print.domain.vo.response.PrintImageResp;
import com.qc.printers.common.print.domain.vo.response.PrinterBaseResp;
import org.apache.commons.lang.StringUtils;

/**
 * 转pdf完成的图片轮询处理器类
 */
public class ImagePdfFinishType implements ImageTypeRStrategy {
    @Override
    public PrinterBaseResp<PrintImageResp> buildResp(PrintImageTypeDto printImageTypeDto) {
        PrinterBaseResp<PrintImageResp> printImageRespPrinterBaseResp = new PrinterBaseResp<>();
        printImageRespPrinterBaseResp.setType(0);
        if (printImageTypeDto.getIsCanGetImage().equals(1) && StringUtils.isNotEmpty(printImageTypeDto.getImageDownloadUrl())) {
            printImageRespPrinterBaseResp.setType(1);
            printImageRespPrinterBaseResp.setData(new PrintImageResp(printImageTypeDto.getId(), printImageTypeDto.getImageDownloadUrl()));
            return printImageRespPrinterBaseResp;
        }
        if (printImageTypeDto.getIsCanGetImage().equals(2)) {
            printImageRespPrinterBaseResp.setType(2);
            printImageRespPrinterBaseResp.setMessage(printImageRespPrinterBaseResp.getMessage());
            return printImageRespPrinterBaseResp;
        }
        return printImageRespPrinterBaseResp;
    }
}
