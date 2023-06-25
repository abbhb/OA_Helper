package com.qc.printers.utils;

import cn.hutool.core.img.ImgUtil;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;



public class ImageUtil {
    public static MultipartFile base64ImageToM(String base64WithHarder) throws IOException {
        BufferedImage image = ImgUtil.toImage(base64WithHarder.replaceFirst("data:image/png;base64,",""));
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write( image, "png", baos );
        //转换为MultipartFile
        MultipartFile multipartFile = new MockMultipartFile("avatar.png","avatar.png","image/png", baos.toByteArray());
        return multipartFile;
    }
}
