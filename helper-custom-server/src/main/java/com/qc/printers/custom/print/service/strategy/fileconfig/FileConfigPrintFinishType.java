package com.qc.printers.custom.print.service.strategy.fileconfig;

import com.qc.printers.custom.print.domain.dto.PrintFileConfigTypeDto;
import com.qc.printers.custom.print.domain.vo.response.PrintFileConfigResp;
import com.qc.printers.custom.print.domain.vo.response.PrinterBaseResp;

/**
 * 打印完成
 */
public class FileConfigPrintFinishType implements FileConfigTypeRStrategy {
    @Override
    public PrinterBaseResp<PrintFileConfigResp> buildResp(PrintFileConfigTypeDto printFileConfigTypeDto) {
        PrinterBaseResp<PrintFileConfigResp> temp = new PrinterBaseResp<>();
        temp.setType(2);
        temp.setMessage("打印已完成,继续打印可以复位！");
        return temp;
    }
}
