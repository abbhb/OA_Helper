package com.qc.printers.common.activiti.constant;

/**
 * 常量参数
 *
 * @author liuguofeng
 * @date 2023/11/04 12:27
 **/
public class Constants {

    /**
     * 文件上传前缀
     */
    public final static String RESOURCE_PREFIX = "upload";

    /**
     * 前端在请求头内传的token,为了模拟登录,
     * 其实Token就是userId
     */
    public final static String TOKEN = "Token";

    /**
     * 发起人key
     */
    public final static String PROCESS_INITIATOR = "initiator";
    public final static String PROCESS_ASSIGNEELEADER_0 = "assigneeLeader0";
    public final static String PROCESS_ASSIGNEELEADER_1 = "assigneeLeader1";
    public final static String PROCESS_ASSIGNEELEADER_2 = "assigneeLeader2";

}
