package com.qc.printers.custom.print.service.strategy;

import com.qc.printers.common.print.domain.dto.PrinterRedis;
import com.qc.printers.custom.print.domain.dto.PrintFileConfigTypeDto;
import com.qc.printers.custom.print.domain.enums.PrintDataRespTypeEnum;
import com.qc.printers.custom.print.domain.vo.response.PrinterBaseResp;
import com.qc.printers.custom.print.service.strategy.fileconfig.FileConfigTypeHandlerFactory;
import com.qc.printers.custom.print.service.strategy.fileconfig.FileConfigTypeRStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class PrintFileConfigHandler extends AbstratePrintDataHandler {
    @Override
    PrintDataRespTypeEnum getDataTypeEnum() {
        return PrintDataRespTypeEnum.FILECONFIG;
    }

    @Override
    public PrinterBaseResp createR(PrinterRedis printerRedis) {
        PrintFileConfigTypeDto fileConfigTypeDto = new PrintFileConfigTypeDto();
        BeanUtils.copyProperties(printerRedis, fileConfigTypeDto);
        FileConfigTypeRStrategy fileConfigTypeRStrategy = FileConfigTypeHandlerFactory.createFileConfigTypeRStrategy(fileConfigTypeDto);
        return fileConfigTypeRStrategy.buildResp(fileConfigTypeDto);
    }
}
