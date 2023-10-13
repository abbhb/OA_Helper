package com.qc.printers.custom.print.service.strategy;

import com.qc.printers.common.print.domain.dto.PrinterRedis;
import com.qc.printers.custom.print.domain.dto.PrintImageTypeDto;
import com.qc.printers.custom.print.domain.enums.PrintDataRespTypeEnum;
import com.qc.printers.custom.print.domain.vo.response.PrinterBaseResp;
import com.qc.printers.custom.print.service.strategy.image.ImageTypeHandlerFactory;
import com.qc.printers.custom.print.service.strategy.image.ImageTypeRStrategy;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

@Component
public class PrintImageHandler extends AbstratePrintDataHandler {
    @Override
    PrintDataRespTypeEnum getDataTypeEnum() {
        return PrintDataRespTypeEnum.IMAGE;
    }

    @Override
    public PrinterBaseResp createR(PrinterRedis printerRedis) {
        PrintImageTypeDto imageTypeDto = new PrintImageTypeDto();
        BeanUtils.copyProperties(printerRedis, imageTypeDto);
        ImageTypeRStrategy imageTypeRStrategy = ImageTypeHandlerFactory.createImageTypeRStrategy(imageTypeDto);
        return imageTypeRStrategy.buildResp(imageTypeDto);
    }
}
