package com.qc.printers.custom.notice.controller;

import com.qc.printers.common.common.CustomException;
import com.qc.printers.common.common.R;
import com.qc.printers.common.common.annotation.NeedToken;
import com.qc.printers.common.common.annotation.PermissionCheck;
import com.qc.printers.common.common.domain.entity.PageData;
import com.qc.printers.common.notice.domain.entity.Notice;
import com.qc.printers.custom.notice.domain.vo.req.NoticeAddReq;
import com.qc.printers.custom.notice.domain.vo.req.NoticeUpdateReq;
import com.qc.printers.custom.notice.domain.vo.resp.NoticeAddResp;
import com.qc.printers.custom.notice.domain.vo.resp.NoticeMangerListResp;
import com.qc.printers.custom.notice.domain.vo.resp.NoticeUserResp;
import com.qc.printers.custom.notice.domain.vo.resp.NoticeViewResp;
import com.qc.printers.custom.notice.service.NoticeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
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
     * 查看全部通知列表
     * 不细分，参考wordpress就是所有的文章都能被管理员编辑，默认带参数，只看我们自己创建的，也可以带参数，看所有人
     * type:1 仅看我
     * type:2 看所有人
     * status:比如已发布，草稿，未发布，全部
     * 均按创建时间降序排序，暂时不做部门负责人绑定用户，
     */
    @PermissionCheck(role = {"superadmin"}, permission = "sys:notice:list")
    @NeedToken
    @ApiOperation(value = "所有人的通知列表", notes = "")
    @GetMapping("/publish_list")
    public R<PageData<NoticeMangerListResp>> publishNoticeList(@ApiParam(value = "1:仅看我，2:看所有人") @RequestParam Integer type,
                                                               @ApiParam(value = "分页参数") @RequestParam Integer pageNum,
                                                               @ApiParam(value = "分页参数") @RequestParam Integer pageSize,
                                                               @ApiParam(value = "状态", required = false) Integer status,
                                                               @ApiParam(value = "搜索", required = false) String search,
                                                               @ApiParam(value = "搜索方式:1:通知名，2：tag暂不支持从内容搜索",
                                                                       required = false) Integer searchType) {
        return R.successOnlyObject(noticeService.publishNoticeList(type, status, pageNum, pageSize, search, searchType));
    }


    /**
     * 仅更新通知接口
     */
    @PermissionCheck(role = {"superadmin"}, permission = "sys:notice:update")
    @NeedToken
    @Transactional
    @ApiOperation(value = "更新通知", notes = "无论是发布还是内容更新，都共用这一个接口")
    @PutMapping("/update")
    public R<String> updateNotice(@RequestBody NoticeUpdateReq noticeUpdateReq) {
        if (noticeUpdateReq.getNotice().getStatus() == null) {
            throw new CustomException("最少包含一种状态");
        }

        NoticeAddReq noticeAddReq = new NoticeAddReq();
        Notice noticeY = noticeUpdateReq.getNotice();
        noticeAddReq.setDeptIds(noticeUpdateReq.getDeptIds());
        if (noticeUpdateReq.getAnnexes() != null && noticeUpdateReq.getAnnexes().size() > 0) {
            noticeY.setIsAnnex(1);
        } else {
            noticeY.setIsAnnex(0);
        }
        noticeAddReq.setNotice(noticeY);
        //更细基本信息
        noticeService.quickUpdateNotice(noticeAddReq);
        //更新内容
        noticeService.updateNoticeContent(noticeUpdateReq);
        return R.successOnlyObject("更新成功");
    }

    /**
     * 删除通知接口
     */
    @PermissionCheck(role = {"superadmin"}, permission = "sys:notice:delete")
    @NeedToken
    @DeleteMapping("/delete/{noticeId}")
    public R<String> deleteNotice(@PathVariable("noticeId") String noticeId) {
        noticeService.deleteNotice(noticeId);
        return R.successOnlyObject("删除成功");
    }

    /**
     * 快速编辑
     */
    @PermissionCheck(role = {"superadmin"}, permission = "sys:notice:update")
    @NeedToken
    @ApiOperation(value = "快速编辑", notes = "无论是发布还是内容更新，都共用这一个接口")
    @PutMapping("/quick_update")
    public R<String> quickUpdateNotice(@RequestBody NoticeAddReq noticeAddReq) {
        noticeService.quickUpdateNotice(noticeAddReq);
        return R.successOnlyObject("更新成功");
    }

    /**
     * 编辑通知-获取通知基本信息
     */
    @PermissionCheck(role = {"superadmin"}, permission = "sys:notice:update")
    @NeedToken
    @ApiOperation(value = "编辑通知-获取通知基本信息", notes = "")
    @GetMapping("/view/edit/{id}")
    public R<NoticeViewResp> viewNotice(@PathVariable("id") String noticeId) {
        return R.successOnlyObject(noticeService.viewNoticeEdit(noticeId));
    }

    // 以下为用户端查看的相关接口

    /**
     * 通知列表
     *
     * @return
     */
    @NeedToken
    @ApiOperation(value = "用户端通知列表接口", notes = "")
    @GetMapping("/list/{urgency}")
    public R<PageData<NoticeUserResp>> getNoticeList(@PathVariable("urgency") Integer urgency, @RequestParam("pageNum") Integer pageNum, @RequestParam("pageSize") Integer pageSize, String tag, Long deptId) {
        return R.successOnlyObject(noticeService.getNoticeList(urgency, pageNum, pageSize, tag, deptId));
    }

}
