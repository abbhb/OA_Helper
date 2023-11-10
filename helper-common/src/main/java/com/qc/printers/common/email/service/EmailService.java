package com.qc.printers.common.email.service;

public interface EmailService {
    /**
     * 异常直接抛出，正常就是空
     *
     * @param to
     * @param subject
     * @param content
     */
    void sendEmail(String to, String subject, String content);

    String getEmailCode(String email);
}
