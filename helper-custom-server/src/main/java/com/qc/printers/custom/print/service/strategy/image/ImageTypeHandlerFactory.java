package com.qc.printers.custom.print.service.strategy.image;

import com.qc.printers.custom.print.domain.dto.PrintImageTypeDto;
import com.qc.printers.custom.print.domain.enums.PrintImageStuEnum;

public class ImageTypeHandlerFactory {
    public static ImageTypeRStrategy createImageTypeRStrategy(PrintImageTypeDto printImageTypeDto) {
        try {
            Class<? extends ImageTypeRStrategy> strategyClass = PrintImageStuEnum.of(printImageTypeDto.getSTU()).getDataClass();
            if (strategyClass != null) {
                return strategyClass.getConstructor(String[].class).newInstance((Object) strategyClass);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
