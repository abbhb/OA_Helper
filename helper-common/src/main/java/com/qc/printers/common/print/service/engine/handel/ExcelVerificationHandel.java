package com.qc.printers.common.print.service.engine.handel;

import com.qc.printers.common.print.domain.enums.FileTypeEnum;
import com.qc.printers.common.print.service.engine.FileVerificationEngine;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class ExcelVerificationHandel  extends FileVerificationEngine {
    @Override
    protected FileTypeEnum getFileTypeEnum() {
        return FileTypeEnum.EXCEL;
    }

    @Override
    protected boolean checkWay(MultipartFile file) {
        return true;
    }
}
