package com.qc.printers.custom.activiti.controller;


import com.qc.printers.common.activiti.entity.dto.workflow.FinishedListDto;
import com.qc.printers.common.activiti.service.ProcessHistoryService;
import com.qc.printers.common.common.R;
import com.qc.printers.common.common.annotation.NeedToken;
import com.qc.printers.common.common.domain.entity.PageData;
import com.qc.printers.common.common.utils.ThreadLocalUtil;
import com.qc.printers.common.user.domain.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


/**
 * 已办任务
 *
 * @author liuguofeng
 * @date 2024/01/08 9:12
 **/
@RequestMapping("/processFinished")
@CrossOrigin("*")
@RestController
public class ProcessFinishedController {

    @Autowired
    private ProcessHistoryService processHistoryService;

    /**
     * 查看我代办的流程
     *
     * @param dto 参数
     */
    @NeedToken
    @GetMapping("/list")
    public R<PageData> list(FinishedListDto dto) {
        User user = ThreadLocalUtil.getCurrentUser();
        dto.setUserId(String.valueOf(user.getId()));
        return R.success(processHistoryService.queryPage(dto));
    }


}
