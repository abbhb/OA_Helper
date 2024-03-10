package com.qc.printers.custom.activiti.controller;


import com.qc.printers.common.activiti.entity.dto.workflow.TodoCompleteDto;
import com.qc.printers.common.activiti.entity.dto.workflow.TodoListDto;
import com.qc.printers.common.activiti.service.ProcessTodoService;
import com.qc.printers.common.common.R;
import com.qc.printers.common.common.annotation.NeedToken;
import com.qc.printers.common.common.domain.entity.PageData;
import com.qc.printers.common.common.utils.ThreadLocalUtil;
import com.qc.printers.common.user.domain.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 待办流程
 **/
@CrossOrigin("*")
@RequestMapping("/processTodo")
@RestController
public class ProcessTodoController {

    @Autowired
    private ProcessTodoService processTodoService;

    /**
     * 查看我代办的流程
     *
     * @param dto 参数
     */
    @NeedToken
    @GetMapping("/list")
    public R<PageData> list(TodoListDto dto) {
        User user = ThreadLocalUtil.getCurrentUser();
        dto.setUserId(String.valueOf(user.getId()));
        dto.setDeptId(String.valueOf(user.getDeptId()));
        return R.success(processTodoService.queryPage(dto));
    }

    /**
     * 获取节点表单
     *
     * @param taskId 任务id
     * @return 表单数据
     */
    @GetMapping("/getNodeForm/{taskId}")
    public R<Map<String, Object>> getNodeForm(@PathVariable String taskId) {
        Map<String, Object> nodeForm = processTodoService.getNodeForm(taskId);
        return R.success(nodeForm);
    }

    /**
     * 审批节点
     *
     * @param dto 参数
     */
    @NeedToken
    @PostMapping("/complete")
    public R<String> complete(@RequestBody TodoCompleteDto dto) {
        User user = ThreadLocalUtil.getCurrentUser();
        dto.setUserId(String.valueOf(user.getId()));
        dto.setDeptId(String.valueOf(user.getDeptId()));
        processTodoService.complete(dto);
        return R.success("审批成功");
    }


}
