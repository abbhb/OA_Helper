package com.qc.printers.custom.chat.controller;


import com.qc.printers.common.chat.domain.dto.MsgReadInfoDTO;
import com.qc.printers.common.chat.domain.vo.request.*;
import com.qc.printers.common.chat.domain.vo.response.ChatMemberListResp;
import com.qc.printers.common.chat.domain.vo.response.ChatMemberStatisticResp;
import com.qc.printers.common.chat.domain.vo.response.ChatMessageReadResp;
import com.qc.printers.common.chat.domain.vo.response.ChatMessageResp;
import com.qc.printers.common.chat.service.ChatService;
import com.qc.printers.common.common.R;
import com.qc.printers.common.common.annotation.FrequencyControl;
import com.qc.printers.common.common.annotation.NeedToken;
import com.qc.printers.common.common.domain.vo.response.CursorPageBaseResp;
import com.qc.printers.common.common.utils.RequestHolder;
import com.qc.printers.common.user.domain.vo.response.ws.ChatMemberResp;
import com.qc.printers.common.user.service.cache.UserCache;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Collection;
import java.util.List;

/**
 * <p>
 * 群聊相关接口
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
public class ChatController {
    @Autowired
    private ChatService chatService;
    @Autowired
    private UserCache userCache;

    @GetMapping("/public/member/page")
    @ApiOperation("群成员列表（废弃）")
    @Deprecated
//    @FrequencyControl(time = 120, count = 20, target = FrequencyControl.Target.IP)
    public R<CursorPageBaseResp<ChatMemberResp>> getMemberPage(@Valid MemberReq request) {
        CursorPageBaseResp<ChatMemberResp> memberPage = chatService.getMemberPage(null, request);
        return R.success(memberPage);
    }


    @GetMapping("/member/list")
    @ApiOperation("房间内的所有群成员列表-@专用（废弃）")
    @Deprecated
    public R<List<ChatMemberListResp>> getMemberList(@Valid ChatMessageMemberReq chatMessageMemberReq) {
        return R.success(chatService.getMemberList(chatMessageMemberReq));
    }


    @GetMapping("public/member/statistic")
    @ApiOperation("群成员人数统计")
    @Deprecated
    public R<ChatMemberStatisticResp> getMemberStatistic() {
        return R.success(chatService.getMemberStatistic());
    }

    @CrossOrigin("*")
    @NeedToken
    @GetMapping("/public/msg/page")
    @ApiOperation("消息列表")
//    @FrequencyControl(time = 120, count = 20, target = FrequencyControl.Target.IP)
    public R<CursorPageBaseResp<ChatMessageResp>> getMsgPage(@Valid ChatMessagePageReq request) {
        CursorPageBaseResp<ChatMessageResp> msgPage = chatService.getMsgPage(request, RequestHolder.get().getUid());
        return R.success(msgPage);
    }

    @CrossOrigin("*")
    @NeedToken
    @PostMapping("/msg")
    @ApiOperation("发送消息")
//    @FrequencyControl(time = 5, count = 3, target = FrequencyControl.Target.UID)
//    @FrequencyControl(time = 30, count = 5, target = FrequencyControl.Target.UID)
//    @FrequencyControl(time = 60, count = 10, target = FrequencyControl.Target.UID)
    public R<ChatMessageResp> sendMsg(@Valid @RequestBody ChatMessageReq request) {//todo 发送给单聊
        Long msgId = chatService.sendMsg(request, RequestHolder.get().getUid());
        //返回完整消息格式，方便前端展示
        return R.success(chatService.getMsgResp(msgId, RequestHolder.get().getUid()));
    }

    @NeedToken
    @PutMapping("/msg/mark")
    @ApiOperation("消息标记")
    @FrequencyControl(time = 10, count = 5, target = FrequencyControl.Target.UID)
    public R<Void> setMsgMark(@Valid @RequestBody ChatMessageMarkReq request) {
        chatService.setMsgMark(RequestHolder.get().getUid(), request);
        return R.success("成功");
    }

    @NeedToken
    @PostMapping("/msg/remove")
    @ApiOperation("消息记录删除，删除后对于本人就跟不存在此条消息一样")
    @FrequencyControl(time = 10, count = 5, target = FrequencyControl.Target.UID)
    public R<Void> setMsgMark(@Valid @RequestBody ChatMessageStateReq request) {
        chatService.setMsgRemove(RequestHolder.get().getUid(), request);
        return R.success("成功");
    }

    /**
     * 只有群管理员能撤回任意消息
     *
     * @param request
     * @return
     */
    @NeedToken
    @PutMapping("/msg/recall")
    @ApiOperation("撤回消息")
//    @FrequencyControl(time = 20, count = 3, target = FrequencyControl.Target.UID)
    public R<Void> recallMsg(@Valid @RequestBody ChatMessageBaseReq request) {
        chatService.recallMsg(RequestHolder.get().getUid(), request);
        return R.success("撤回成功");
    }

    @GetMapping("/msg/read/page")
    @NeedToken
    @ApiOperation("消息的已读未读列表")
    public R<CursorPageBaseResp<ChatMessageReadResp>> getReadPage(@Valid ChatMessageReadReq request) {
        Long uid = RequestHolder.get().getUid();
        return R.success(chatService.getReadPage(uid, request));
    }

    @PostMapping("/msg/read")
    @NeedToken
    @ApiOperation("获取消息的已读未读总数")
    public R<Collection<MsgReadInfoDTO>> getReadInfo(@Valid @RequestBody ChatMessageReadInfoReq request) {
        // todo: c.q.p.c.common.GlobalExceptionHandler    : nested exception is org.apache.ibatis.builder.BuilderException: The expression 'coll' evaluated to a null value.
        Long uid = RequestHolder.get().getUid();
        return R.success(chatService.getMsgReadInfo(uid, request));
    }

    @PutMapping("/msg/read")
    @NeedToken
    @ApiOperation("消息阅读上报")
    public R<Void> msgRead(@Valid @RequestBody ChatMessageMemberReq request) {
        Long uid = RequestHolder.get().getUid();
        chatService.msgRead(uid, request);
        return R.success("11");
    }
}

