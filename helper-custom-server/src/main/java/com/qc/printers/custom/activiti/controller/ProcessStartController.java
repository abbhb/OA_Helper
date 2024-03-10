package com.qc.printers.custom.activiti.controller;


import com.qc.printers.common.activiti.entity.dto.workflow.StartListDto;
import com.qc.printers.common.activiti.entity.dto.workflow.StartProcessDto;
import com.qc.printers.common.activiti.entity.vo.workflow.HistoryRecordVo;
import com.qc.printers.common.activiti.service.ProcessHistoryService;
import com.qc.printers.common.activiti.service.ProcessStartService;
import com.qc.printers.common.common.R;
import com.qc.printers.common.common.annotation.NeedToken;
import com.qc.printers.common.common.domain.entity.PageData;
import com.qc.printers.common.common.utils.ThreadLocalUtil;
import com.qc.printers.common.user.domain.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 流程启动
 *
 * @author liuguofeng
 * @date 2023/11/04 12:07
 **/
@RequestMapping("/processStart")
@CrossOrigin("*")
@RestController
public class ProcessStartController {
    @Autowired
    private ProcessStartService processStartService;

    @Autowired
    private ProcessHistoryService processHistoryService;

    /**
     * 我发起的任务列表
     *
     * @param dto 参数
     */
    @NeedToken
    @GetMapping("/list")
    public R<PageData> list(StartListDto dto) {
        User user = ThreadLocalUtil.getCurrentUser();
        dto.setUserId(String.valueOf(user.getId()));
        return R.success(processStartService.queryPage(dto));
    }

    /**
     * 启动流程
     *
     * @param dto 启动流程参数
     * @return 结果
     */
    @NeedToken
    @PostMapping("/start")
    public R<String> start(@RequestBody StartProcessDto dto) {
        User user = ThreadLocalUtil.getCurrentUser();
        processStartService.startProcess(dto, String.valueOf(user.getId()));
        return R.success("启动成功");
    }

    /**
     * 查询审批进度
     *
     * @param instanceId 流程实例id
     * @return 审批记录
     */
    @GetMapping("/getHistoryRecord")
    public R<List<HistoryRecordVo>> getHistoryRecord(String instanceId) {
        List<HistoryRecordVo> list = processHistoryService.getHistoryRecord(instanceId);
        return R.success(list);
    }

    /**
     * 查询流程图信息(高亮信息)
     *
     * @param instanceId 流程实例id
     * @return 流程图信息
     */
    @GetMapping("/getHighlightNodeInfo")
    public R<Map<String, Object>> getHighlightNodeInfo(String instanceId) {
        Map<String, Object> result = processHistoryService.getHighlightNodeInfo(instanceId);
        return R.success(result);
    }

    /**
     * 获取主表单信息
     *
     * @param instanceId 流程实例id
     * @return 主表单数据
     */
    @GetMapping("/getMainFormInfo")
    public R<Map<String, Object>> getMainFormInfo(String instanceId) {
        Map<String, Object> list = processHistoryService.getMainFormInfo(instanceId);
        return R.success(list);
    }

    /**
     * 删除流程实例
     *
     * @param instanceId 流程实例id
     */
    @DeleteMapping("/delete")
    public R<String> delete(String instanceId) {
        processStartService.delete(instanceId);
        return R.success("删除成功");
    }
}
