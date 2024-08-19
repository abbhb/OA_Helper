package com.qc.printers.custom.chat.controller;


import com.qc.printers.common.chat.domain.vo.request.ContactFriendReq;
import com.qc.printers.common.chat.domain.vo.request.ContactRemovedReq;
import com.qc.printers.common.chat.domain.vo.response.ChatRoomResp;
import com.qc.printers.common.chat.service.ChatService;
import com.qc.printers.common.chat.service.ContactService;
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
import org.springframework.web.bind.annotation.*;

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
    private ContactService contactService;
    @Autowired
    private ChatService chatService;
    @Autowired
    private RoomAppService roomService;

    /**
     * 获取不到会话可能是不显示删除了，去房间表同步在创建就好
     * 问题确认，删除会话不会影响系统功能，只是用户自己不显示而已，但是bug出在删除的哪个用户如果不是删除会话记录，
     * 即同步删除聊天信息新建的时候应该带上最后msg_id和消息时间，去room表拉取就行，否则会报错，同上
     * 在删除会话（连同消息记录）时得处理消息表，加字段逻辑删除还是真删除？
     * @param request
     * @return
     */
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

    @NeedToken
    @DeleteMapping("/public/contact")
    @ApiOperation("删除会话")
    public R<Boolean> removedContact(@Valid ContactRemovedReq request) {
        Long uid = RequestHolder.get().getUid();
        contactService.removeContact(uid,request.getRoomId());
        return R.success(true);
    }

}

