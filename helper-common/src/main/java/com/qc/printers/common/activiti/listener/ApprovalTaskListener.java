package com.qc.printers.common.activiti.listener;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.qc.printers.common.activiti.constant.TaskDeleteType;
import com.qc.printers.common.activiti.service.ProcessStartService;
import lombok.extern.slf4j.Slf4j;
import org.activiti.engine.delegate.*;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ApprovalTaskListener implements ExecutionListener, ApplicationContextAware {

    private static ProcessStartService processStartService;
    @Override
    public void notify(DelegateExecution delegateExecution) {
        try {
            log.info("审批任务监听器 {}", delegateExecution.getEventName());
            if ("end".equals(delegateExecution.getEventName())) {
                // 获取审批结果变量

                ObjectNode approvalResult = (ObjectNode)delegateExecution.getVariable(String.format("%s_formData", delegateExecution.getCurrentActivityId()));
                // {"comment":"True","remark":"555"}
                String comment = approvalResult.get("comment").asText();
                log.info("审批结果变量 {},type {}", approvalResult,approvalResult.getClass());
                if ("False".equals(comment)) {
                    // 审批不通过，终止流程实例
                    processStartService.delete(delegateExecution.getProcessInstanceId(), TaskDeleteType.BuTongGuo);
                }
            }
        }catch (Exception e){
            log.error("审批任务监听器异常",e);
        }

    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        processStartService =applicationContext.getBean(ProcessStartService.class);
    }
}
