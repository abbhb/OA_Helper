package com.qc.printers.common.activiti.constant;

/**
 * 节点状态
 *
 * @author liuguofeng
 * @date 2023/11/22 09:01
 **/
public class NodeStatus {
    /**
     * 已完成的节点
     */
    public final static int EXECUTED = 1;

    /**
     * 未完成的节点
     */
    public final static int UNFINISHED = 2;

    /**
     * 不通过的节点
     */
    public final static int ERROR = 3;
}
