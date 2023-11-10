package com.qc.printers.common.vailcode.service;

import com.qc.printers.common.common.CustomException;
import com.qc.printers.common.common.MyString;
import com.qc.printers.common.common.utils.RedisUtils;
import com.qc.printers.common.vailcode.domain.entity.Captcha;
import com.qc.printers.common.vailcode.utils.CaptchaUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class VailcodeService {
    /**
     * 300s刷新一次
     *
     * @param captcha
     * @return
     */
    public Captcha getCaptcha(Captcha captcha) {
        //参数校验
        CaptchaUtils.checkCaptcha(captcha);
        //获取画布的宽高
        int canvasWidth = captcha.getCanvasWidth();
        int canvasHeight = captcha.getCanvasHeight();
        //获取阻塞块的宽高/半径
        int blockWidth = captcha.getBlockWidth();
        int blockHeight = captcha.getBlockHeight();
        int blockRadius = captcha.getBlockRadius();
        //获取干扰阻塞块的宽高/半径
        int XblockWidth = captcha.getBlockWidth() - 5;
        int XblockHeight = captcha.getBlockHeight();
        int XblockRadius = captcha.getBlockRadius() + 2;
        //获取资源图
        BufferedImage canvasImage = CaptchaUtils.getBufferedImage(captcha.getPlace());
        //调整原图到指定大小
        canvasImage = CaptchaUtils.imageResize(canvasImage, canvasWidth, canvasHeight);
        //随机生成阻塞块坐标
        int blockX = CaptchaUtils.getNonceByRange(blockWidth, canvasWidth - blockWidth - 10);
        int blockY = CaptchaUtils.getNonceByRange(10, canvasHeight - blockHeight + 1);
        //阻塞块
        BufferedImage blockImage = new BufferedImage(blockWidth, blockHeight, BufferedImage.TYPE_4BYTE_ABGR);
        //新建的图像根据轮廓图颜色赋值，源图生成遮罩
        CaptchaUtils.cutByTemplate(canvasImage, blockImage, blockWidth, blockHeight, blockRadius, blockX, blockY);
        // 干扰块
        //随机生成阻塞块坐标
        int XblockX = CaptchaUtils.getNonceByRange(XblockWidth, canvasWidth - XblockWidth - 10);
        int XblockY = CaptchaUtils.getNonceByRange(10, canvasHeight - XblockHeight + 1);
        //阻塞块
        BufferedImage XblockImage = new BufferedImage(XblockWidth, XblockHeight, BufferedImage.TYPE_4BYTE_ABGR);
        //新建的图像根据轮廓图颜色赋值，源图生成遮罩
        CaptchaUtils.cutByTemplate(canvasImage, XblockImage, XblockWidth, XblockHeight, XblockRadius, XblockX, XblockY);
        // 移动横坐标
        String nonceStr = UUID.randomUUID().toString().replaceAll("-", "");
        // 缓存
        RedisUtils.set(MyString.CAPTCHA_KEY + nonceStr, blockX, 300, TimeUnit.SECONDS);
        //设置返回参数
        captcha.setNonceStr(nonceStr);
        captcha.setBlockY(blockY);
        captcha.setBlockSrc(CaptchaUtils.toBase64(blockImage, "png"));
        captcha.setCanvasSrc(CaptchaUtils.toBase64(canvasImage, "png"));
        return captcha;
    }

    public String checkImageCode(String nonceStr, String value) {
        Integer s = RedisUtils.get(MyString.CAPTCHA_KEY + nonceStr, Integer.class);
        if (s == null) {
            throw new CustomException("验证码已过期");
        }
        RedisUtils.del(MyString.CAPTCHA_KEY + nonceStr);
        // 失败还是成功都只让用一次
        // 根据移动距离判断验证是否成功
        if (Math.abs(Integer.parseInt(value) - s) > 5) {
            throw new CustomException("验证失败，请控制拼图对齐缺口");
        }
        String success_token = UUID.randomUUID().toString();
        RedisUtils.set(MyString.CAPTCHA_Success + success_token, success_token, 60, TimeUnit.SECONDS);
        return success_token;
    }
}
