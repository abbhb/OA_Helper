package com.qc.printers.custom.chat.controller;



import com.qc.printers.common.common.R;
import com.qc.printers.common.common.annotation.NeedToken;
import com.qc.printers.common.common.domain.vo.request.CursorPageBaseReq;
import com.qc.printers.common.common.domain.vo.request.PageBaseReq;
import com.qc.printers.common.common.domain.vo.response.CursorPageBaseResp;
import com.qc.printers.common.common.domain.vo.response.PageBaseResp;
import com.qc.printers.common.common.utils.RequestHolder;
import com.qc.printers.common.user.domain.vo.request.friend.FriendApplyReq;
import com.qc.printers.common.user.domain.vo.request.friend.FriendApproveReq;
import com.qc.printers.common.user.domain.vo.request.friend.FriendCheckReq;
import com.qc.printers.common.user.domain.vo.request.friend.FriendDeleteReq;
import com.qc.printers.common.user.domain.vo.response.friend.FriendApplyResp;
import com.qc.printers.common.user.domain.vo.response.friend.FriendCheckResp;
import com.qc.printers.common.user.domain.vo.response.friend.FriendResp;
import com.qc.printers.common.user.domain.vo.response.friend.FriendUnreadResp;
import com.qc.printers.custom.chat.service.FriendService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

/**
 * <p>
 *     /user/friend/ to  /chat-friend
 * 好友相关接口
 * </p>
 *
 * @author <a href="https://github.com/zongzibinbin">abin</a>
 * @since 2023-07-16
 */
@RestController
@RequestMapping("/chat-friend")
@Api(tags = "好友相关接口")
@Slf4j
@CrossOrigin("*")
public class FriendController {
    @Resource
    private FriendService friendService;

    @GetMapping("/check")
    @NeedToken
    @ApiOperation("批量判断是否是自己好友")
    public R<FriendCheckResp> check(@Valid FriendCheckReq request) {
        Long uid = RequestHolder.get().getUid();
        return R.success(friendService.check(uid, request));
    }

    @PostMapping("/apply")
    @NeedToken
    @ApiOperation("申请好友")
    public R<String> apply(@Valid @RequestBody FriendApplyReq request) {
        Long uid = RequestHolder.get().getUid();
        friendService.apply(uid, request);
        return R.success("成功");
    }

    @DeleteMapping()
    @NeedToken
    @ApiOperation("删除好友")
    public R<String> delete(@Valid @RequestBody FriendDeleteReq request) {
        Long uid = RequestHolder.get().getUid();
        friendService.deleteFriend(uid, request.getTargetUid());
        return R.success("成功");
    }

    @GetMapping("/apply/page")
    @NeedToken
    @ApiOperation("好友申请列表")
    public R<PageBaseResp<FriendApplyResp>> page(@Valid PageBaseReq request) {
        Long uid = RequestHolder.get().getUid();
        return R.success(friendService.pageApplyFriend(uid, request));
    }

    @GetMapping("/apply/unread")
    @NeedToken
    @ApiOperation("申请未读数")
    public R<FriendUnreadResp> unread() {
        Long uid = RequestHolder.get().getUid();
        return R.success(friendService.unread(uid));
    }

    @PutMapping("/apply")
    @NeedToken
    @ApiOperation("审批同意")
    public R<String> applyApprove(@Valid @RequestBody FriendApproveReq request) {
        friendService.applyApprove(RequestHolder.get().getUid(), request);
        return R.success("同意");
    }

    @GetMapping("/page")
    @NeedToken
    @ApiOperation("联系人列表")
    public R<CursorPageBaseResp<FriendResp>> friendList(@Valid CursorPageBaseReq request) {
        Long uid = RequestHolder.get().getUid();
        return R.success(friendService.friendList(uid, request));
    }
}

