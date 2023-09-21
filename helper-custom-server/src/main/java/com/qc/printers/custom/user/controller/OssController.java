package com.qc.printers.custom.user.controller;

import com.qc.printers.common.common.R;
import com.qc.printers.common.common.annotation.NeedToken;
import com.qc.printers.common.common.utils.RequestHolder;
import com.qc.printers.common.common.utils.oss.domain.OssResp;
import com.qc.printers.common.user.domain.vo.request.oss.UploadUrlReq;
import com.qc.printers.custom.user.service.OssService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * Description: chat-oss控制层
 * 目前chat的文件为单独的
 * Author: <a href="https://github.com/zongzibinbin">abin</a>
 * Date: 2023-06-20
 */

@RestController
@RequestMapping("/chat-oss")
@Api(tags = "chat-oss相关接口")
public class OssController {

    @Autowired
    private OssService ossService;

    @NeedToken
    @CrossOrigin("*")
    @GetMapping("/upload/url")
    @ApiOperation("获取临时上传链接")
    public R<OssResp> getUploadUrl(@Valid UploadUrlReq req) {
        return R.success(ossService.getUploadUrl(RequestHolder.get().getUid(), req));
    }


}
