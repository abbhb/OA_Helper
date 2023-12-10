package com.qc.printers.custom.common.controller;

import com.qc.printers.custom.common.service.ThymeleafService;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Slf4j
@RequestMapping("/ext-thymeleaf")
@Api("公共接口")
@Controller//此处不用restcontroller是因为这个controller专用于thymeleaf 返回
public class ThymeleafController {
    @Autowired
    private ThymeleafService thymeleafService;

    /**
     * 使用了xxx第三方登录，存在邮箱但是不存在密码
     *
     * @return
     */
    @RequestMapping("/re-new-password")
    public String reNewPassword(String oneTimeCode, Model model) {
        return thymeleafService.reNewPassword(oneTimeCode, model);
    }

    /**
     * error
     */
    @RequestMapping("/error")
    public String error(String msg, Model model) {
        model.addAttribute("error", msg);
        return "error";
    }

}
