package com.qc.printers.custom.oauth.controller;

import com.qc.printers.common.common.R;
import com.qc.printers.common.common.annotation.NeedToken;
import com.qc.printers.common.config.OauthConfig;
import com.qc.printers.custom.oauth.domain.dto.Authorize;
import com.qc.printers.custom.oauth.domain.vo.CanAuthorize;
import com.qc.printers.custom.oauth.domain.vo.req.AgreeLoginReq;
import com.qc.printers.custom.oauth.domain.vo.req.AgreeReq;
import com.qc.printers.custom.oauth.domain.vo.resp.AgreeLoginResp;
import com.qc.printers.custom.oauth.domain.vo.resp.AgreeResp;
import com.qc.printers.custom.oauth.domain.vo.resp.MeResp;
import com.qc.printers.custom.oauth.domain.vo.resp.TokenResp;
import com.qc.printers.custom.oauth.service.OauthService;
import com.qc.printers.custom.oauth.service.strategy.GetAccessTokenHandel;
import com.qc.printers.custom.oauth.service.strategy.GetAccessTokenHandelFactory;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@RestController//@ResponseBody+@Controller
@RequestMapping("/oauth2.0")
@Slf4j
@CrossOrigin("*")
@Api("oauth2.0")
public class OauthController {

    @Autowired
    private OauthService oauthService;

    @Autowired
    private OauthConfig oauthConfig;

    @Autowired
    private GetAccessTokenHandelFactory getAccessTokenHandelFactory;

    @GetMapping("/authorize")
    public void authorize(HttpServletResponse response, @RequestParam("response_type") String responseType, @RequestParam("client_id") String clientId, @RequestParam("redirect_uri") String redirectUri, @RequestParam("state") String state, String scope) throws IOException {
        // 没有scope就只剩原始的me接口，只能拿到openid
        CanAuthorize canAuthorize = oauthService.isCanAuthorize(responseType, clientId, redirectUri, state, scope);
        if (canAuthorize.isCan()) {
            String pinjie = "";
            pinjie = pinjie + "&client_id=" + clientId;
            pinjie = pinjie + "&redirect_uri=" + redirectUri;
            pinjie = pinjie + "&state=" + state;
            if (StringUtils.isNotEmpty(scope)) {
                pinjie = pinjie + "&scope=" + scope;
            }

            response.sendRedirect(oauthConfig.getFrontAddress() + "/?which=auth&response_type=code" + pinjie + "#/show");
            return;
        }
        // 不成功
        response.sendRedirect(oauthConfig.getFrontAddress() + "/?which=error&msg=" + canAuthorize.getMsg() + "#/show");
    }

    // 两种状态的确认授权，一种是已经登录的状态
    @PostMapping("/agree")
    @NeedToken
    @ApiOperation(value = "统一授权")
    public R<AgreeResp> agree(@RequestBody AgreeReq agreeReq) {
        return R.success(oauthService.agree(agreeReq));
    }

    /**
     * 未登录时的鉴权接口
     *
     * @param agreeReq
     * @return
     */
    @PostMapping("/agree_login")
    @ApiOperation(value = "统一授权还没有登录的情况")
    public R<AgreeLoginResp> agreeLogin(@RequestBody AgreeLoginReq agreeReq) {
        return R.success(oauthService.agreeLogin(agreeReq));
    }

    @GetMapping("/token")
    public TokenResp authorizeCodeTOAccessToken(String code, String grant_type, String client_id, String client_secret, String redirect_uri, String refresh_token) {
        Authorize authorize = new Authorize();
        authorize.setCode(code);
        authorize.setRedirectUri(redirect_uri);
        authorize.setClientId(client_id);
        authorize.setClientSecret(client_secret);
        authorize.setGrantType(grant_type);
        authorize.setRefreshToken(refresh_token);
        GetAccessTokenHandel instance = getAccessTokenHandelFactory.getInstance(authorize.getGrantType());
        if (instance == null) {
            TokenResp tokenResp = new TokenResp();
            tokenResp.setCode(100000);
            tokenResp.setMsg("缺少参数response_type或response_type非法");
            return tokenResp;
        }
        return instance.getAccessToken(authorize);

    }

    /**
     * 这个接口都能访问，无需授权
     *
     * @param accessToken
     * @return
     */
    @GetMapping("/me")
    public MeResp me(@RequestParam(name = "access_token") String accessToken) {
        return oauthService.me(accessToken);
    }
    // todo:其余信息接口

    @GetMapping("/get_client_name")
    public R<String> getClientName(@RequestParam(name = "client_id") String clientId) {
        return R.successOnlyObject(oauthService.getClientName(clientId));
    }
}
