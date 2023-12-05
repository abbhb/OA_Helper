package com.qc.printers.common.vailcode.controller;

import com.qc.printers.common.common.R;
import com.qc.printers.common.vailcode.config.VailCodeConfig;
import com.qc.printers.common.vailcode.domain.entity.Captcha;
import com.qc.printers.common.vailcode.service.VailcodeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequestMapping("/vail_code")
public class VailcodeController {
    @Autowired
    private VailcodeService vailcodeService;

    @Autowired
    private VailCodeConfig vailCodeConfig;

    /**
     * 获取验证码拼图
     * // todo:没有更好的限流思路
     *
     * @param captcha
     * @return
     */
    @CrossOrigin("*")
    @PostMapping("/get_captcha")
    public R<Captcha> getCaptcha(@RequestBody Captcha captcha) {
        if (!vailCodeConfig.isImageVailUse()) {
            captcha.setIsUse(0);
            return R.successOnlyObject(captcha);
        }
        captcha.setIsUse(1);
        return R.success(vailcodeService.getCaptcha(captcha));
    }

    /**
     * @param captcha
     * @return successCode
     */
    @CrossOrigin("*")
    @PostMapping("/check_captcha_code")
    public R<String> checkImageCode(@RequestBody Captcha captcha) {
        String s = vailcodeService.checkImageCode(captcha.getNonceStr(), captcha.getValue());
        return R.successOnlyObject(s);
    }

}
