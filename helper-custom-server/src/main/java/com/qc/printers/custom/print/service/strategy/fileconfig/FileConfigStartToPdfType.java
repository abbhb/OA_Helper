package com.qc.printers.custom.print.service.strategy.fileconfig;

import com.qc.printers.custom.print.domain.dto.PrintFileConfigTypeDto;
import com.qc.printers.custom.print.domain.vo.response.PrintFileConfigResp;
import com.qc.printers.custom.print.domain.vo.response.PrinterBaseResp;

/**
 * 用户刚开始转pdf
 */
public class FileConfigStartToPdfType implements FileConfigTypeRStrategy {
    @Override
    public PrinterBaseResp<PrintFileConfigResp> buildResp(PrintFileConfigTypeDto printFileConfigTypeDto) {
        PrinterBaseResp<PrintFileConfigResp> printImageRespPrinterBaseResp = new PrinterBaseResp<>();
        printImageRespPrinterBaseResp.setType(0);
        return printImageRespPrinterBaseResp;
    }
}
