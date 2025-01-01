package com.qc.printers.custom.print.service.strategy.image;

import com.qc.printers.custom.print.domain.dto.PrintImageTypeDto;
import com.qc.printers.common.print.domain.vo.response.PrintImageResp;
import com.qc.printers.common.print.domain.vo.response.PrinterBaseResp;

public class ImageErrorType implements ImageTypeRStrategy {
    @Override
    public PrinterBaseResp<PrintImageResp> buildResp(PrintImageTypeDto printImageTypeDto) {
        //失败了，提示用户重试！
        PrinterBaseResp<PrintImageResp> temp = new PrinterBaseResp<>();
        temp.setType(2);
        temp.setData(null);
        temp.setMessage(printImageTypeDto.getMessage());
        return temp;
    }
}
