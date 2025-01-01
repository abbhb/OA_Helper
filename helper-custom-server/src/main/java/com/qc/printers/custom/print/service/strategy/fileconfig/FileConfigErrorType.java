package com.qc.printers.custom.print.service.strategy.fileconfig;


import com.qc.printers.custom.print.domain.dto.PrintFileConfigTypeDto;
import com.qc.printers.common.print.domain.vo.response.PrintFileConfigResp;
import com.qc.printers.common.print.domain.vo.response.PrinterBaseResp;

public class FileConfigErrorType implements FileConfigTypeRStrategy {
    @Override
    public PrinterBaseResp<PrintFileConfigResp> buildResp(PrintFileConfigTypeDto printFileConfigTypeDto) {
        //失败了，提示用户重试！
        PrinterBaseResp<PrintFileConfigResp> temp = new PrinterBaseResp<>();
        temp.setType(2);
        temp.setData(null);
        temp.setMessage(printFileConfigTypeDto.getMessage());
        return temp;
    }
}
