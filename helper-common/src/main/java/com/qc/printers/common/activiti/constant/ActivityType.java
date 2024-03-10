package com.qc.printers.common.activiti.constant;

/**
 * 网关类型
 *
 * @author liuguofeng
 * @date 2023/11/21 16:45
 **/
public class ActivityType {

    /**
     * 开始节点
     */
    public final static String START_EVENT = "startEvent";


    /**
     * 用户节点
     */
    public final static String USER_TASK = "userTask";

    /**
     * 并行网关
     */
    public final static String PARALLEL_GATEWAY = "parallelGateway";

    /**
     * 包容网关
     */
    public final static String INCLUSIVE_GATEWAY = "inclusiveGateway";

    /**
     * 互斥网关
     */
    public final static String EXCLUSIVE_GATEWAY = "exclusiveGateway";
}
