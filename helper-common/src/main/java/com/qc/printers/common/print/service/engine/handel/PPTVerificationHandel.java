package com.qc.printers.common.print.service.engine.handel;

import com.qc.printers.common.print.domain.enums.FileTypeEnum;
import com.qc.printers.common.print.service.engine.FileVerificationEngine;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class PPTVerificationHandel extends FileVerificationEngine {
    @Override
    protected FileTypeEnum getFileTypeEnum() {
        return FileTypeEnum.PPT;
    }

    @Override
    protected boolean checkWay(MultipartFile file) {
        return true;
    }
}
