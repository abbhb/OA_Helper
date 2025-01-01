package com.qc.printers.custom.print.service.strategy.fileconfig;

import com.qc.printers.custom.print.domain.dto.PrintFileConfigTypeDto;
import com.qc.printers.common.print.domain.vo.response.PrintFileConfigResp;
import com.qc.printers.common.print.domain.vo.response.PrinterBaseResp;

/**
 * 开始打印
 */
public class FileConfigStartPrintType implements FileConfigTypeRStrategy {
    @Override
    public PrinterBaseResp<PrintFileConfigResp> buildResp(PrintFileConfigTypeDto printFileConfigTypeDto) {
        PrinterBaseResp<PrintFileConfigResp> printImageRespPrinterBaseResp = new PrinterBaseResp<>();

        if (!printFileConfigTypeDto.getPageNums().equals(0)) {
            printImageRespPrinterBaseResp.setType(1);
            printImageRespPrinterBaseResp.setData(new PrintFileConfigResp(printFileConfigTypeDto.getId(), printFileConfigTypeDto.getNeedPrintPagesIndex(), printFileConfigTypeDto.getNeedPrintPagesEndIndex(), printFileConfigTypeDto.getName()));
            return printImageRespPrinterBaseResp;
        }
        printImageRespPrinterBaseResp.setType(2);
        printImageRespPrinterBaseResp.setMessage("状态异常");
        return printImageRespPrinterBaseResp;
    }
}
