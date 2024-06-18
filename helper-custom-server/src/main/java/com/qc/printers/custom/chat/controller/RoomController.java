package com.qc.printers.custom.chat.controller;


import com.qc.printers.common.chat.domain.vo.request.*;
import com.qc.printers.common.chat.domain.vo.request.admin.AdminAddReq;
import com.qc.printers.common.chat.domain.vo.request.admin.AdminRevokeReq;
import com.qc.printers.common.chat.domain.vo.request.groupbase.GroupAvatarReq;
import com.qc.printers.common.chat.domain.vo.request.groupbase.GroupNameReq;
import com.qc.printers.common.chat.domain.vo.request.member.MemberExitReq;
import com.qc.printers.common.chat.domain.vo.response.ChatMemberListResp;
import com.qc.printers.common.chat.domain.vo.response.MemberResp;
import com.qc.printers.common.chat.service.IGroupMemberService;
import com.qc.printers.common.chat.service.RoomAppService;
import com.qc.printers.common.common.R;
import com.qc.printers.common.common.annotation.NeedToken;
import com.qc.printers.common.common.domain.vo.request.IdReqVO;
import com.qc.printers.common.common.domain.vo.response.CursorPageBaseResp;
import com.qc.printers.common.common.domain.vo.response.IdRespVO;
import com.qc.printers.common.common.utils.RequestHolder;
import com.qc.printers.common.user.domain.vo.response.ws.ChatMemberResp;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * <p>
 * 房间相关接口
 * </p>
 *
 * @author <a href="https://github.com/abbhb">qc2003</a>
 * @since 2023-06-19
 */
@CrossOrigin("*")
@RestController
@RequestMapping("/room")
@Api(tags = "聊天室相关接口")
@Slf4j
public class RoomController {
    @Autowired
    private RoomAppService roomService;

    @Autowired
    private IGroupMemberService groupMemberService;

    @GetMapping("/public/group")
    @NeedToken
    @ApiOperation("群组详情")
    public R<MemberResp> groupDetail(@Valid IdReqVO request) {
        Long uid = RequestHolder.get().getUid();
        return R.success(roomService.getGroupDetail(uid, request.getId()));
    }


    @GetMapping("/public/group/member/page")
    @NeedToken
    @ApiOperation("群成员列表")
    public R<CursorPageBaseResp<ChatMemberResp>> getMemberPage(@Valid MemberReq request) {
        return R.success(roomService.getMemberPage(request));
    }


    @GetMapping("/group/member/list")
    @NeedToken
    @ApiOperation("房间内的所有群成员列表-@专用")
    public R<List<ChatMemberListResp>> getMemberList(@Valid ChatMessageMemberReq request) {
        return R.success(roomService.getMemberList(request));
    }

    @DeleteMapping("/group/member")
    @NeedToken
    @ApiOperation("移除成员")
    public R<String> delMember(@Valid MemberDelReq request) {
        Long uid = RequestHolder.get().getUid();
        roomService.delMember(uid, request);
        return R.success("移除成功");
    }

    @DeleteMapping("/group/member/exit")
    @NeedToken
    @ApiOperation("退出群聊")
    public R<Boolean> exitGroup(@Valid MemberExitReq request) {
        Long uid = RequestHolder.get().getUid();
        groupMemberService.exitGroup(uid, request);
        return R.success(true);
    }

    @PostMapping("/group")
    @NeedToken
    @ApiOperation("新增群组")
    public R<IdRespVO> addGroup(@Valid @RequestBody GroupAddReq request) {
        Long uid = RequestHolder.get().getUid();
        Long roomId = roomService.addGroup(uid, request);
        return R.success(IdRespVO.id(roomId));
    }

    @PostMapping("/group/member")
    @NeedToken
    @ApiOperation("邀请好友")
    public R<Void> addMember(@Valid @RequestBody MemberAddReq request) {
        Long uid = RequestHolder.get().getUid();
        roomService.addMember(uid, request);
        return R.success("");
    }
    @NeedToken
    @PutMapping("/group/admin")
    @ApiOperation("添加管理员")
    public R<Boolean> addAdmin(@Valid @RequestBody AdminAddReq request) {
        Long uid = RequestHolder.get().getUid();
        groupMemberService.addAdmin(uid, request);
        return R.success("");
    }

    @NeedToken
    @DeleteMapping("/group/admin")
    @ApiOperation("撤销管理员")
    public R<Boolean> revokeAdmin(@Valid @RequestBody AdminRevokeReq request) {
        Long uid = RequestHolder.get().getUid();
        groupMemberService.revokeAdmin(uid, request);
        return R.success(true);
    }

    @NeedToken
    @PutMapping("/group/name")
    @ApiOperation("修改群聊name")
    public R<Boolean> putName(@Valid @RequestBody GroupNameReq request) {
        Long uid = RequestHolder.get().getUid();
        roomService.putName(uid, request);
        return R.success("设置成功");
    }
    @NeedToken
    @PutMapping("/group/avatar")
    @ApiOperation("修改群聊avatar")
    public R<Boolean> putAvatar(@Valid @RequestBody GroupAvatarReq request) {
        Long uid = RequestHolder.get().getUid();
        roomService.putAvatar(uid, request);
        return R.success("设置成功");
    }
}

