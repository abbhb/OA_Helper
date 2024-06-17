package com.qc.printers.custom.chat.controller;


import com.qc.printers.common.chat.domain.vo.request.ContactFriendReq;
import com.qc.printers.common.chat.domain.vo.response.ChatRoomResp;
import com.qc.printers.common.chat.service.ChatService;
import com.qc.printers.common.chat.service.RoomAppService;
import com.qc.printers.common.common.R;
import com.qc.printers.common.common.annotation.NeedToken;
import com.qc.printers.common.common.domain.vo.request.CursorPageBaseReq;
import com.qc.printers.common.common.domain.vo.request.IdReqVO;
import com.qc.printers.common.common.domain.vo.response.CursorPageBaseResp;
import com.qc.printers.common.common.utils.RequestHolder;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * <p>
 * 会话相关接口
 * </p>
 *
 * @author <a href="https://github.com/zongzibinbin">abin</a>
 * @since 2023-03-19
 */
@CrossOrigin("*")
@RestController
@RequestMapping("/chat")
@Api(tags = "聊天室相关接口")
@Slf4j
public class ContactController {
    @Autowired
    private ChatService chatService;
    @Autowired
    private RoomAppService roomService;

    @GetMapping("/public/contact/page")
    @NeedToken
    @ApiOperation("会话列表")
    public R<CursorPageBaseResp<ChatRoomResp>> getRoomPage(@Valid CursorPageBaseReq request) {
        Long uid = RequestHolder.get().getUid();
        return R.success(roomService.getContactPage(request, uid));
    }

    @GetMapping("/public/contact/detail")
    @NeedToken
    @ApiOperation("会话详情")
    public R<ChatRoomResp> getContactDetail(@Valid IdReqVO request) {
        Long uid = RequestHolder.get().getUid();
        return R.success(roomService.getContactDetail(uid, request.getId()));
    }

    @NeedToken
    @GetMapping("/public/contact/detail/friend")
    @ApiOperation("会话详情(联系人列表发消息用)")
    public R<ChatRoomResp> getContactDetailByFriend(@Valid ContactFriendReq request) {
        Long uid = RequestHolder.get().getUid();
        return R.success(roomService.getContactDetailByFriend(uid, request.getUid()));
    }
}

