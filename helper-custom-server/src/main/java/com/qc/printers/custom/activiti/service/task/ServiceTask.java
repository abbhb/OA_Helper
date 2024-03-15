package com.qc.printers.custom.activiti.service.task;

import org.springframework.stereotype.Component;

@Component("serviceTask")
public class ServiceTask {
    public void hello(String initiator) {
        System.out.println("===myBean执行====");
        System.out.println("你好：" + initiator);//打印   你好：中国
    }
}
