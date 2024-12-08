package com.qc.printers.custom.user.controller;

import com.qc.printers.common.common.R;
import com.qc.printers.common.common.annotation.NeedToken;
import com.qc.printers.custom.user.domain.vo.request.UserFrontConfigReq;
import com.qc.printers.custom.user.domain.vo.response.app.AppState;
import com.qc.printers.custom.user.service.UserFrontConfigService;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController//@ResponseBody+@Controller
@RequestMapping("/user_front_config")
@Api("和用户前端配置相关的接口")
@CrossOrigin("*")
@Slf4j
public class UserFrontConfigController {

    @Autowired
    private UserFrontConfigService userFrontConfigService;

    @NeedToken
    @GetMapping("/get_user_front_config")
    public R<AppState> getUserFrontConfig() {
        return R.successOnlyObject(userFrontConfigService.getUserFrontConfig());
    }

    @NeedToken
    @PostMapping("/set_user_front_config")
    public R<String> setUserFrontConfig(@RequestBody UserFrontConfigReq userFrontConfigReq) {
        userFrontConfigService.setUserFrontConfig(userFrontConfigReq.getAppState());
        //否则就新增
        return R.success("更新成功");
    }
}
