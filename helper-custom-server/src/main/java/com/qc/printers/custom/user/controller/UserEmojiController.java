package com.qc.printers.custom.user.controller;

import com.qc.printers.common.common.R;
import com.qc.printers.common.common.annotation.NeedToken;
import com.qc.printers.common.common.domain.vo.request.IdReqVO;
import com.qc.printers.common.common.domain.vo.response.IdRespVO;
import com.qc.printers.common.common.utils.RequestHolder;
import com.qc.printers.common.user.domain.vo.request.user.UserEmojiReq;
import com.qc.printers.common.user.domain.vo.response.user.UserEmojiResp;
import com.qc.printers.custom.user.service.UserEmojiService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 用户表情包
 *
 * @author: WuShiJie
 * @createTime: 2023/7/3 14:21
 */
@RestController
@RequestMapping("/user/emoji")
@Api(tags = "用户表情包管理相关接口")
public class UserEmojiController {

    /**
     * 用户表情包 Service
     */
    @Autowired
    private UserEmojiService userEmojiService;


    /**
     * 表情包列表
     *
     * @return 表情包列表
     * @author WuShiJie
     * @createTime 2023/7/3 14:46
     **/
    @CrossOrigin("*")
    @NeedToken
    @GetMapping("/list")
    @ApiOperation("表情包列表")
    public R<List<UserEmojiResp>> getEmojisPage() {
        return R.success(userEmojiService.list(RequestHolder.get().getUid()));
    }


    /**
     * 新增表情包
     *
     * @param req 用户表情包
     * @return 表情包
     * @author WuShiJie
     * @createTime 2023/7/3 14:46
     **/
    @CrossOrigin("*")
    @PostMapping("/add")
    @NeedToken
    @ApiOperation("新增表情包")
    public R<IdRespVO> insertEmojis(@Valid @RequestBody UserEmojiReq req) {
        return userEmojiService.insert(req, RequestHolder.get().getUid());
    }

    /**
     * 删除表情包
     *
     * @return 删除结果
     * @author WuShiJie
     * @createTime 2023/7/3 14:46
     **/
    @CrossOrigin("*")
    @DeleteMapping("/delete")
    @NeedToken
    @ApiOperation("删除表情包")
    public R<Void> deleteEmojis(@Valid @RequestBody IdReqVO reqVO) {
        userEmojiService.remove(reqVO.getId(), RequestHolder.get().getUid());
        return R.success("");
    }
}
