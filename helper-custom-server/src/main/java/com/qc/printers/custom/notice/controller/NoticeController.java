package com.qc.printers.custom.notice.controller;

import com.qc.printers.common.common.CustomException;
import com.qc.printers.common.common.R;
import com.qc.printers.common.common.annotation.NeedToken;
import com.qc.printers.common.common.annotation.PermissionCheck;
import com.qc.printers.custom.notice.domain.vo.req.NoticeAddReq;
import com.qc.printers.custom.notice.domain.vo.req.NoticeUpdateReq;
import com.qc.printers.custom.notice.domain.vo.resp.NoticeAddResp;
import com.qc.printers.custom.notice.service.NoticeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController//@ResponseBody+@Controller
@RequestMapping("/notice")
@Slf4j
@CrossOrigin("*")
@Api("notice")
public class NoticeController {

    @Autowired
    private NoticeService noticeService;

    /**
     * 添加通知
     */
    @PostMapping("/add")
    @PermissionCheck(role = {"superadmin"}, permission = "sys:notice:add")
    @NeedToken
    @ApiOperation(value = "创建新通知", notes = "")
    public R<NoticeAddResp> addNotice(@RequestBody NoticeAddReq noticeAddReq) {
        return R.success(noticeService.addNotice(noticeAddReq));
    }

    /**
     * 发布并保存通知
     * 调用完更新的服务再调用发布的服务即可
     */
    @PermissionCheck(role = {"superadmin"}, permission = "sys:notice:publish")
    @NeedToken
    @ApiOperation(value = "更新通知", notes = "")
    @PostMapping("/publish")
    public R<String> publishNotice(@RequestBody NoticeUpdateReq noticeUpdateReq) {
        if (noticeUpdateReq.getNotice().getId() == null) {
            throw new CustomException("ID为空发布个g啊");
        }
        noticeService.updateNotice(noticeUpdateReq);
        noticeService.publishNotice(noticeUpdateReq.getNotice().getId());
        return R.successOnlyObject("发布成功");
    }

    /**
     * 定时发布并保存通知，勾选了定时发布，携带定时的信息
     */

    /**
     * 禁止查看按钮接口
     */

    /**
     * 基本信息变更接口，就刚创建时哪些信息，比如可见性等等
     */


    /**
     * 仅更新通知接口
     */
    @PermissionCheck(role = {"superadmin"}, permission = "sys:notice:update")
    @NeedToken
    @ApiOperation(value = "更新通知", notes = "")
    @PutMapping("/update")
    public R<String> updateNotice(@RequestBody NoticeUpdateReq noticeUpdateReq) {
        return R.successOnlyObject(noticeService.updateNotice(noticeUpdateReq));
    }

    /**
     * 删除通知接口
     */
    @DeleteMapping("/delete")
    public String deleteNotice(String noticeId) {
//        return noticeService.deleteNotice(noticeId);
        return null;
    }

    /**
     * 发布端通知列表，可带参数，比如已发布，草稿，未发布，全部   均按时间排序，暂时不做部门负责人绑定用户，
     */
}
