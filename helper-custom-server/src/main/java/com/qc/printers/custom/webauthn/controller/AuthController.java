package com.qc.printers.custom.webauthn.controller;

import com.qc.printers.common.common.R;
import com.qc.printers.common.common.annotation.NeedToken;
import com.qc.printers.common.common.utils.JWTUtil;
import com.qc.printers.common.common.utils.RedisUtils;
import com.qc.printers.common.webauthn.entity.UserPasskey;
import com.qc.printers.common.webauthn.vo.req.ActionPasskeyRegistrationReq;
import com.qc.printers.custom.user.domain.vo.response.LoginRes;
import com.qc.printers.custom.user.service.UserService;
import com.qc.printers.custom.webauthn.service.PasskeyAuthorizationService;
import com.yubico.webauthn.AssertionRequest;
import com.yubico.webauthn.data.PublicKeyCredentialCreationOptions;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.jdbc.Null;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.concurrent.TimeUnit;

@RestController//@ResponseBody+@Controller
@RequestMapping("/authorization/passkey")
@Api("和Passkey相关的接口")
@CrossOrigin("*")
@Slf4j
public class AuthController {
    private final PasskeyAuthorizationService passkeyAuthorizationService;


    public AuthController(PasskeyAuthorizationService passkeyAuthorizationService) {
        this.passkeyAuthorizationService = passkeyAuthorizationService;
    }

    @NeedToken
    @GetMapping("/registration/options")
    public R<String> getPasskeyRegistrationOptions() {
        return R.successOnlyObject(passkeyAuthorizationService.startPasskeyRegistration());
    }

    @NeedToken
    @PostMapping("/registration")
    public R<UserPasskey> verifyPasskeyRegistration(@RequestBody String json) {
        log.info("credential--",json);
        return R.successOnlyObject(passkeyAuthorizationService.finishPasskeyRegistration(json));
    }

    @NeedToken
    @GetMapping("/registration/list")
    public R<List<UserPasskey>> listPasskeyRegistrations() {
        return R.successOnlyObject(passkeyAuthorizationService.listPasskeyRegistrations());
    }

    @NeedToken
    @PostMapping("/registration/action")
    public R<Void> actionPasskeyRegistration(@RequestBody ActionPasskeyRegistrationReq data) {
        return R.success(passkeyAuthorizationService.actionPasskeyRegistration(data));
    }

    @GetMapping("/assertion/options")
    public R<String> getPasskeyAssertionOptions(HttpServletRequest httpServletRequest) {
        return R.successOnlyObject(passkeyAuthorizationService.startPasskeyAssertion(httpServletRequest.getSession().getId()));
    }

    @PostMapping("/assertion")
    public R<LoginRes> verifyPasskeyAssertion(HttpServletRequest httpServletRequest, @RequestBody String credential) {
        var id = passkeyAuthorizationService.finishPasskeyAssertion(httpServletRequest.getSession().getId(), credential);
        // Login the user with `id`
        String token = JWTUtil.getToken(String.valueOf(id));
        RedisUtils.set(token, String.valueOf(id), 12 * 3600L, TimeUnit.SECONDS);
        return R.success(
                LoginRes.builder()
                        .token(token)
                        .toSetPassword(0)
                .build()
        );
    }
}
