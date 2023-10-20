package com.qc.printers.common.common.constant;

/**
 * @author zhongzb create on 2021/06/10
 */
public interface MQConstant {
    /**
     * pdf转缩略图
     */
    String SEND_PDF_IMAGE_TOPIC = "print_pdf_toimage_send_msg";
    String SEND_PDF_IMAGE_GROUP = "print_pdf_toimage_send_msg_group";

    //用来接受返回
    String SEND_PDF_IMAGE_R_TOPIC = "print_pdf_toimage_send_msg_r";
    String SEND_PDF_IMAGE_R_GROUP = "print_pdf_toimage_send_msg_r_group";
    /**
     * 转pdf发送mq
     */
    String SEND_FILE_TOPDF_TOPIC = "print_filetopdf_send_msg";
    String SEND_FILE_TOPDF_GROUP = "print_filetopdf_send_msg_group";

    //用来接受返回
    String SEND_FILE_TOPDF_R_TOPIC = "print_filetopdf_send_msg_r";
    String SEND_FILE_TOPDF_R_GROUP = "print_filetopdf_send_msg_r_group";
    /**
     * 打印任务发送mq
     */
    String SEND_PRINT_TOPIC = "print_send_msg";
    String SEND_PRINT_GROUP = "print_send_msg_group";

    String SEND_PRINT_R_TOPIC = "print_send_msg_r";
    String SEND_PRINT_R_GROUP = "print_send_msg_r_group";
    /**
     * 消息发送mq
     */
    String SEND_MSG_TOPIC = "chat_send_msg";
    String SEND_MSG_GROUP = "chat_send_msg_group";

    /**
     * push用户
     */
    String PUSH_TOPIC = "websocket_push";
    String PUSH_GROUP = "websocket_push_group";

    /**
     * (授权完成后)登录信息mq
     */
    String LOGIN_MSG_TOPIC = "user_login_send_msg";
    String LOGIN_MSG_GROUP = "user_login_send_msg_group";

    /**
     * 扫码成功 信息发送mq
     */
    String SCAN_MSG_TOPIC = "user_scan_send_msg";
    String SCAN_MSG_GROUP = "user_scan_send_msg_group";
}
