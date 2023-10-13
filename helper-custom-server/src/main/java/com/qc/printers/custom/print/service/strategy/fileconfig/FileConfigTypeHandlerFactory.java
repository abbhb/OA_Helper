package com.qc.printers.custom.print.service.strategy.fileconfig;

import com.qc.printers.custom.print.domain.dto.PrintFileConfigTypeDto;
import com.qc.printers.custom.print.domain.enums.PrintFileConfigStuEnum;

public class FileConfigTypeHandlerFactory {
    public static FileConfigTypeRStrategy createFileConfigTypeRStrategy(PrintFileConfigTypeDto printFileConfigTypeDto) {
        try {
            Class<? extends FileConfigTypeRStrategy> strategyClass = PrintFileConfigStuEnum.of(printFileConfigTypeDto.getSTU()).getDataClass();
            if (strategyClass != null) {
                return strategyClass.getConstructor(String[].class).newInstance((Object) strategyClass);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
