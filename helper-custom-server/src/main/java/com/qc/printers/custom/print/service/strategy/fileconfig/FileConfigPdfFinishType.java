package com.qc.printers.custom.print.service.strategy.fileconfig;

import com.qc.printers.custom.print.domain.dto.PrintFileConfigTypeDto;
import com.qc.printers.custom.print.domain.vo.response.PrintFileConfigResp;
import com.qc.printers.custom.print.domain.vo.response.PrinterBaseResp;

/**
 * 转pdf完成
 */
public class FileConfigPdfFinishType implements FileConfigTypeRStrategy {
    @Override
    public PrinterBaseResp<PrintFileConfigResp> buildResp(PrintFileConfigTypeDto printFileConfigTypeDto) {
        PrinterBaseResp<PrintFileConfigResp> printImageRespPrinterBaseResp = new PrinterBaseResp<>();
        printImageRespPrinterBaseResp.setType(1);
        printImageRespPrinterBaseResp.setData(new PrintFileConfigResp(printFileConfigTypeDto.getId(), printFileConfigTypeDto.getNeedPrintPagesIndex(), printFileConfigTypeDto.getPageNums(), printFileConfigTypeDto.getName()));
        return printImageRespPrinterBaseResp;
    }
}
