package com.qc.printers.common.print.service.engine.handel;

import com.qc.printers.common.common.CustomException;
import com.qc.printers.common.print.domain.enums.FileTypeEnum;
import com.qc.printers.common.print.service.engine.FileVerificationEngine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
@Slf4j
@Component
public class PhotoVerificationHandel extends FileVerificationEngine {
    @Override
    protected FileTypeEnum getFileTypeEnum() {
        return FileTypeEnum.IMAGE;
    }

    @Override
    protected boolean checkWay(MultipartFile file) {
        // 获取系统中的临时目录
        Path tempDir = Paths.get(System.getProperty("java.io.tmpdir"));

        // 临时文件使用 UUID 随机命名
        Path tempFile = tempDir.resolve(Paths.get(UUID.randomUUID().toString()));

        try {
            // copy 到临时文件
            file.transferTo(tempFile);
            // 使用 ImageIO 读取文件
            BufferedImage read = ImageIO.read(tempFile.toFile());
            if (read == null) {
                throw new CustomException("图片无法解析，请放入pdf重新尝试!");
            }
            // 图片正常
            return true;

        } catch (IOException e) {
            log.error("ERROR","error:{}",e);
            throw new CustomException("图片无法解析，请放入pdf重新尝试!");
        } finally {
            // 始终删除临时文件
            try {
                Files.delete(tempFile);
            } catch (IOException e) {
                log.error("ERROR","临时文件删除失败，问题不大");
            }
        }
    }
}
