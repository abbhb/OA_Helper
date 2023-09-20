package com.qc.printers.custom.chat.controller;


import com.qc.printers.common.chat.domain.vo.request.*;
import com.qc.printers.common.chat.domain.vo.response.ChatMemberListResp;
import com.qc.printers.common.chat.domain.vo.response.MemberResp;
import com.qc.printers.common.chat.service.RoomAppService;
import com.qc.printers.common.common.R;
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
 * @author <a href="https://github.com/zongzibinbin">abin</a>
 * @since 2023-03-19
 */
@CrossOrigin("*")
@RestController
@RequestMapping("/room")
@Api(tags = "聊天室相关接口")
@Slf4j
public class RoomController {
    @Autowired
    private RoomAppService roomService;

    @GetMapping("/public/group")
    @ApiOperation("群组详情")
    public R<MemberResp> groupDetail(@Valid IdReqVO request) {
        Long uid = RequestHolder.get().getUid();
        return R.success(roomService.getGroupDetail(uid, request.getId()));
    }


    @GetMapping("/public/group/member/page")
    @ApiOperation("群成员列表")
    public R<CursorPageBaseResp<ChatMemberResp>> getMemberPage(@Valid MemberReq request) {
        return R.success(roomService.getMemberPage(request));
    }

    @GetMapping("/group/member/list")
    @ApiOperation("房间内的所有群成员列表-@专用")
    public R<List<ChatMemberListResp>> getMemberList(@Valid ChatMessageMemberReq request) {
        return R.success(roomService.getMemberList(request));
    }

    @DeleteMapping("/group/member")
    @ApiOperation("移除成员")
    public R<Void> delMember(@Valid @RequestBody MemberDelReq request) {
        Long uid = RequestHolder.get().getUid();
        roomService.delMember(uid, request);
        return R.success("");
    }

    @PostMapping("/group")
    @ApiOperation("新增群组")
    public R<IdRespVO> addGroup(@Valid @RequestBody GroupAddReq request) {
        Long uid = RequestHolder.get().getUid();
        Long roomId = roomService.addGroup(uid, request);
        return R.success(IdRespVO.id(roomId));
    }

    @PostMapping("/group/member")
    @ApiOperation("邀请好友")
    public R<Void> addMember(@Valid @RequestBody MemberAddReq request) {
        Long uid = RequestHolder.get().getUid();
        roomService.addMember(uid, request);
        return R.success("");
    }
}

