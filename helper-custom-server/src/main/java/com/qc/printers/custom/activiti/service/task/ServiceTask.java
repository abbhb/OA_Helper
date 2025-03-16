package com.qc.printers.custom.activiti.service.task;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.qc.printers.common.signin.domain.entity.SigninLogAskLeave;
import com.qc.printers.common.signin.domain.entity.SigninRenewal;
import com.qc.printers.common.signin.domain.resp.FaceFileResp;
import com.qc.printers.common.signin.service.SigninLogAskLeaveService;
import com.qc.printers.common.signin.service.SigninLogService;
import com.qc.printers.common.signin.service.SigninUserDataMangerService;
import com.qc.printers.common.user.domain.dto.UserInfoBaseExtDto;
import com.qc.printers.custom.user.service.UserService;
import com.qc.printers.utils.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.impl.persistence.entity.VariableInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Component("serviceTask")
@Slf4j
public class ServiceTask {
    @Autowired
    private SigninUserDataMangerService signinUserDataMangerService;

    @Autowired
    private UserService userService;

    @Autowired
    private SigninLogAskLeaveService signinLogAskLeaveService;

    @Autowired
    private SigninLogService signinLogService;

    public void hello(String initiator) {
        System.out.println("===myBean执行====");
        System.out.println("你好：" + initiator);//打印   你好：中国
    }


    @Transactional
    public void askLeaveisqingjia(DelegateExecution execution) {
        System.out.println("===myBean执行====");
        String initiator = (String) execution.getVariable("initiator");
        String qishiriqi = (String) execution.getVariable("date90842");
        String jiezhiriqi = (String) execution.getVariable("date29019");
        String qishishijian = (String) execution.getVariable("time88479");
        String jiezhishijian = (String) execution.getVariable("time23031");
        String reason = (String) execution.getVariable("textarea28633");
        // 请假类型 1:病假 2:事假
        String leixin = (String) execution.getVariable("radio96982");
        System.out.println("你好：" + initiator);//打印   你好：中国
        // initiator为发起人
        String qishi = qishiriqi + " " + qishishijian;
        String jiezhi = jiezhiriqi + " " + jiezhishijian;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime start_ = LocalDateTime.parse(qishi, formatter);
        LocalDateTime end_ = LocalDateTime.parse(jiezhi, formatter);
        String processInstanceId = execution.getProcessInstanceId();
        SigninLogAskLeave signinLogAskLeave = new SigninLogAskLeave();
        signinLogAskLeave.setUserId(Long.valueOf(initiator));
        signinLogAskLeave.setStartTime(start_);
        signinLogAskLeave.setEndTime(end_);
        signinLogAskLeave.setAskLeaveAboutActId(processInstanceId);
        signinLogAskLeave.setAskLeaveReason(reason);
        if (leixin.equals("1")){
            leixin = "病假";
        }else if (leixin.equals("2")){
            leixin = "事假";
        }
        signinLogAskLeave.setAskLeaveLeaveType(leixin);
        signinLogAskLeaveService.addAskLeave(signinLogAskLeave);
    }

    /**
     * 补签任务
     * @param execution
     */
    @Transactional
    public void renewalSignin(DelegateExecution execution) {
        System.out.println("===补签任务执行====");
        String initiator = (String) execution.getVariable("initiator");

        List<SigninRenewal> signinRenewals = JsonUtils.toList((String) execution.getVariable("bq_signin_list_json"), SigninRenewal.class);
        for (SigninRenewal signinRenewal : signinRenewals) {
            signinLogService.replacementVisaApprovalByService(Long.valueOf(initiator),signinRenewal.getRenewalTime(),execution.getProcessInstanceId(),signinRenewal.getRenewalReason());
        }


    }



    /**
     * 更新人脸
     *
     * @param execution    传入该变量用于操作工作流的对象
     * @param needDataName 其实就是图片上传附件对应data的id
     */
    @Transactional
    public void updateSigninFaceByUser(DelegateExecution execution, String needDataName) {
//        String processInstanceBusinessKey = execution.getProcessInstanceBusinessKey();
//        String formatDatakey = String.format("%s_formData", processInstanceBusinessKey);
        try {
            // 发起人用户id
            String initiator = (String) execution.getVariable("initiator");
            // 直接返回对应组件的值,这里应该是个ArrayNode
            ArrayNode variable = (ArrayNode) execution.getVariable(needDataName);
            List<FaceFileResp> faceFileResps = new ArrayList<>();
            for (JsonNode jsonNode : variable) {
                FaceFileResp obj = JsonUtils.toObj(jsonNode.toString(), FaceFileResp.class);
                faceFileResps.add(obj);
            }
            signinUserDataMangerService.UpdateUserFaceByUser(Long.valueOf(initiator), faceFileResps.get(0).getUrl());
        } catch (Exception e) {
            log.error("更新人脸任务执行失败:Exception:{},传入组件id:{}", e, needDataName);
        }
    }

    @Transactional
    public void updateUserInfoExt(DelegateExecution execution) {
        try {
            // 发起人用户id
            String userId = (String) execution.getVariable("initiator");
            // 直接返回对应组件的值,这里应该是个ArrayNode
            UserInfoBaseExtDto obj1 = JsonUtils.toObj((String) execution.getVariable("userinfo_ext_data"), UserInfoBaseExtDto.class);
            userService.updateUserInfoExt(Long.valueOf(userId), obj1);
        } catch (Exception e) {
            log.error("更新用户额外信息任务执行失败:Exception:{}", e);
        }
    }


}
