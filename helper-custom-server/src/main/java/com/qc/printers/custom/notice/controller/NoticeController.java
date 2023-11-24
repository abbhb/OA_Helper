package com.qc.printers.custom.notice.controller;

import com.qc.printers.common.common.CustomException;
import com.qc.printers.common.common.R;
import com.qc.printers.common.common.annotation.NeedToken;
import com.qc.printers.common.common.annotation.PermissionCheck;
import com.qc.printers.common.common.domain.entity.PageData;
import com.qc.printers.custom.notice.domain.vo.req.NoticeAddReq;
import com.qc.printers.custom.notice.domain.vo.req.NoticeUpdateReq;
import com.qc.printers.custom.notice.domain.vo.resp.NoticeAddResp;
import com.qc.printers.custom.notice.domain.vo.resp.NoticeMangerListResp;
import com.qc.printers.custom.notice.service.NoticeService;
import com.qc.printers.custom.notice.service.strategy.NoticeUpdateHandelFactory;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
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

    @Autowired
    private NoticeUpdateHandelFactory noticeUpdateHandelFactory;

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
     * 基本信息变更接口，就刚创建时哪些信息，比如可见性等等
     */
    @PermissionCheck(role = {"superadmin"}, permission = "sys:notice:update")
    @NeedToken
    @ApiOperation(value = "更新通知基本信息", notes = "")
    @PutMapping("/update_basic")
    public R<String> updateNoticeBasic(@RequestBody NoticeAddReq noticeAddReq) {
        noticeService.updateNoticeBasic(noticeAddReq);
        return R.successOnlyObject("更新成功");
    }


    /**
     * 仅更新通知接口
     */
    @PermissionCheck(role = {"superadmin"}, permission = "sys:notice:update")
    @NeedToken
    @ApiOperation(value = "更新通知", notes = "无论是发布还是内容更新，都共用这一个接口")
    @PutMapping("/update")
    public R<String> updateNotice(@RequestBody NoticeUpdateReq noticeUpdateReq) {
        if (noticeUpdateReq.getNotice().getStatus() == null) {
            throw new CustomException("最少包含一种状态");
        }
        String s = noticeUpdateHandelFactory.getInstance(noticeUpdateReq.getNotice().getStatus()).updateNotice(noticeUpdateReq);
        return R.successOnlyObject(s);
    }

    /**
     * 删除通知接口
     */
    @PermissionCheck(role = {"superadmin"}, permission = "sys:notice:delete")
    @NeedToken
    @DeleteMapping("/delete")
    public String deleteNotice(String noticeId) {
        noticeService.deleteNotice(noticeId);
        return "删除成功";
    }

}
