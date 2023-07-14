package com.qc.printers.controller;

import com.qc.printers.service.RedirectService;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController//@ResponseBody+@Controller
@RequestMapping("/redirect")
@Api("第三方认证回调接口")
@CrossOrigin("*")
@Slf4j
public class RedirectController {
    private final RedirectService redirectService;

    @Autowired
    public RedirectController(RedirectService redirectService) {
        this.redirectService = redirectService;
    }


//    @GetMapping("/en")
//    @ApiOperation(value = "enroom回调api")
//    public R<UserResult> enRedirect(String code){
//        //直接返回user下的login
//        log.info("code = {}",code);
//        if (StringUtils.isEmpty(code)){
//            return R.error("空");
//        }
//        return redirectService.enRedirect(code);
//    }

//    @PostMapping("/firsten")
//    @ApiOperation(value = "首次通过en认证的账号本地化")
//    public R<UserResult> firstEN(@RequestBody Map<String,Object> data){
//        //直接返回user下的login
//        log.info("data = {}",data);
//        if (data.isEmpty()){
//            return R.error("你倒是填写表单啊");
//        }
//        if (StringUtils.isEmpty((String) data.get("trId"))){
//            return R.error("你倒是填写表单啊");
//        }
//        if (StringUtils.isEmpty((String) data.get("type"))){
//            return R.error("你倒是填写表单啊");
//        }
//        if (StringUtils.isEmpty((String) data.get("username"))){
//            return R.error("你倒是填写表单啊");
//        }
//        if (StringUtils.isEmpty((String) data.get("password"))){
//            return R.error("你倒是填写表单啊");
//        }
//        Long trId = Long.valueOf((String) data.get("trId"));
//        Integer type = Integer.valueOf((String) data.get("type"));
//        String username = (String) data.get("username");
//        String password = (String) data.get("password");
//
//        return redirectService.firstEN(trId,type,username,password);
//    }
}
