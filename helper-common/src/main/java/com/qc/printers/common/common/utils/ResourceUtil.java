package com.qc.printers.common.common.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Random;

public class ResourceUtil {

    public InputStream getResource(String fileName) throws IOException {
        InputStream in = this.getClass().getClassLoader().getResourceAsStream(fileName);
        return in;
    }


    public InputStream getRandomCaptchaImageResource() throws IOException {
        int randomNumber = 1 + new Random().nextInt(31); // 生成1到31的随机数
        String imageName = String.format("wallpaper_%d.jpg", randomNumber);
        InputStream in = getResource("captchaImage/"+imageName);
        return in;
    }

}
