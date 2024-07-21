package com.qc.printers.custom.chat.controller;

import com.qc.printers.common.chat.domain.vo.request.ChatMessagePageReq;
import com.qc.printers.common.chat.domain.vo.request.SystemMessageConfirmReq;
import com.qc.printers.common.chat.domain.vo.response.ChatMessageResp;
import com.qc.printers.common.chat.domain.vo.response.SystemMessageResp;
import com.qc.printers.common.common.R;
import com.qc.printers.common.common.annotation.FrequencyControl;
import com.qc.printers.common.common.annotation.NeedToken;
import com.qc.printers.common.common.domain.vo.response.CursorPageBaseResp;
import com.qc.printers.common.common.utils.RequestHolder;
import com.qc.printers.custom.chat.service.SystemMessageService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.websocket.server.PathParam;
import java.util.List;

@CrossOrigin("*")
@RestController
@RequestMapping("/system-message")
@Api(tags = "系统消息相关接口")
@Slf4j
public class SystemMessageController {

    @Autowired
    private SystemMessageService systemMessageService;
    /**
     * 倒序，未读的新的靠前，其他的放后面新的在前老的灾后
     * @return
     */
    @CrossOrigin("*")
    @NeedToken
    @GetMapping("/list")
    @ApiOperation("消息列表")
    @FrequencyControl(time = 60, count = 20, target = FrequencyControl.Target.UID)
    public R<List<SystemMessageResp>> list() {
        List<SystemMessageResp> list = systemMessageService.list();
        return R.success(list);
    }

    @CrossOrigin("*")
    @NeedToken
    @GetMapping("/noreadCount")
    @ApiOperation("未读条数")
    public R<Integer> noreadCount() {
        return R.success(systemMessageService.noreadCount());
    }



    @CrossOrigin("*")
    @NeedToken
    @PostMapping("/read/{id}/{type}")
    @ApiOperation("阅读上报-阅读和阅读并删除")
    public R<String> read(@PathVariable Long id,@PathVariable Integer type) {
        return R.success(systemMessageService.read(id,type));
    }


    @CrossOrigin("*")
    @NeedToken
    @PostMapping("/readbatch/{type}")
    @ApiOperation("阅读上报-阅读和阅读并删除")
    public R<String> read(@Valid @RequestBody SystemMessageConfirmReq data, @PathVariable Integer type) {
        return R.success(systemMessageService.readBatch(data.getIds(),type));
    }



}
