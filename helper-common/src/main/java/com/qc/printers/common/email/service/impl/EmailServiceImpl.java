package com.qc.printers.common.email.service.impl;

import com.qc.printers.common.common.CustomException;
import com.qc.printers.common.common.MyString;
import com.qc.printers.common.common.utils.RedisUtils;
import com.qc.printers.common.common.utils.VerCodeGenerateUtil;
import com.qc.printers.common.email.service.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class EmailServiceImpl implements EmailService {
    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private TemplateEngine templateEngine;

    @Override
    public void sendEmail(String to, String subject, String content) {
        SimpleMailMessage message = new SimpleMailMessage();
//        message.setFrom("www@AI-EN-Datacom.localdomain");
        message.setFrom("3482238110@qq.com");
        if (StringUtils.isEmpty(to)) {
            throw new CustomException("？请输入邮箱");
        }
        message.setTo(to);

        message.setSubject(subject);
        message.setText(content);
        try {
            mailSender.send(message);
        } catch (Exception e) {
            e.printStackTrace();
            throw new CustomException("服务网络异常，请联系运维!");
        }

    }

    @Override
    public String getEmailCode(String email) {
        // 验证码
        String verCode = VerCodeGenerateUtil.generateVerCode();
        //创建邮件正文
        Context context = new Context();
        context.setVariable("verifyCode", Arrays.asList(verCode.split("")));
        //将模块引擎内容解析成html字符串
        String emailContent = templateEngine.process("verificationEmail", context);
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            // 设置收件人、主题等
            helper.setTo(email);
            helper.setFrom("3482238110@qq.com");
            helper.setSubject("来自Easy_OA,您本次的验证码是");
            helper.setText(emailContent, true);
            RedisUtils.set(MyString.email_code + email, verCode, 5 * 60, TimeUnit.SECONDS);
            mailSender.send(mimeMessage);
        } catch (MessagingException messagingException) {
            log.error(messagingException.getMessage());
            messagingException.printStackTrace();
            throw new CustomException("服务网络异常，请联系运维!");
        }
        // 验证密码存入redis
        return "请前往邮箱查看验证码！";
    }
}
