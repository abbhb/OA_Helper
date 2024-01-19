package com.qc.printers.custom.oauth.controller;

import com.qc.printers.common.common.R;
import com.qc.printers.common.common.annotation.NeedToken;
import com.qc.printers.common.common.annotation.PermissionCheck;
import com.qc.printers.common.config.OauthConfig;
import com.qc.printers.common.oauth.domain.entity.SysOauth;
import com.qc.printers.custom.oauth.domain.dto.Authorize;
import com.qc.printers.custom.oauth.domain.vo.CanAuthorize;
import com.qc.printers.custom.oauth.domain.vo.req.AgreeLoginReq;
import com.qc.printers.custom.oauth.domain.vo.req.AgreeReq;
import com.qc.printers.custom.oauth.domain.vo.resp.*;
import com.qc.printers.custom.oauth.service.OauthService;
import com.qc.printers.custom.oauth.service.strategy.GetAccessTokenHandel;
import com.qc.printers.custom.oauth.service.strategy.GetAccessTokenHandelFactory;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

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


    //todo:注册的默认用户名

    /**
     * state不为强制，提供则返回
     *
     * @param response
     * @param responseType
     * @param clientId
     * @param redirectUri
     * @param state
     * @param scope
     * @throws IOException
     */
    @GetMapping("/authorize")
    public void authorize(HttpServletResponse response, @RequestParam("response_type") String responseType, @RequestParam("client_id") String clientId, @RequestParam(value = "redirect_uri", required = false) String redirectUri, String state, String scope) throws IOException {
        // 静态回调的情况下,传入的回调地址就不重要了,传了直接扔掉,直接取该客户端绑定的回调地址
        String redirectUriEnd = oauthService.getEndRedirectUri(clientId, redirectUri);
        // 没有scope就只剩原始的me接口，只能拿到openid
        CanAuthorize canAuthorize = oauthService.isCanAuthorize(responseType, clientId, redirectUriEnd, state, scope);
        if (canAuthorize.isCan()) {
            String pinjie = "";
            pinjie = pinjie + "&client_id=" + clientId;
            pinjie = pinjie + "&redirect_uri=" + redirectUriEnd;
            if (StringUtils.isNotEmpty(state)) {
                pinjie = pinjie + "&state=" + state;
            }
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
        String redirectUriEnd = oauthService.getEndRedirectUri(client_id, redirect_uri);

        Authorize authorize = new Authorize();
        authorize.setCode(code);
        authorize.setRedirectUri(redirectUriEnd);
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


    /**
     * get_user_info
     * oauth提供获取用户信息的接口，需要用户授权
     * 用户没给get_user_info授权就无法获取这些信息
     *
     * @param accessToken
     * @param openid
     * @param cilentId
     * @return
     */
    @GetMapping("/get_user_info")
    public OauthUserInfoResp getUserInfo(@RequestParam(name = "access_token") String accessToken, @RequestParam(name = "openid") String openid, @RequestParam(name = "oauth_consumer_key") String cilentId) {
        return oauthService.getUserInfo(accessToken, openid, cilentId);
    }

    /**
     * 兼容信息获取
     * 通过header来传输access_token
     *
     * @return
     */
    @GetMapping("/get_user_info_header")
    public OauthUserInfoResp getUserInfoHeader(HttpServletRequest request) {
        return oauthService.getUserInfoHeader(request);
    }

    /**
     * logout接口，用于注销该access_token
     * 暂时不做
     *
     * @return
     */

    @NeedToken
    @PermissionCheck(role = "superadmin", permission = "sys:oauth:list")
    @GetMapping("list")
    public R<List<SysOauth>> list() {
        return R.success(oauthService.list());
    }

    @NeedToken
    @PermissionCheck(role = "superadmin", permission = "sys:oauth:delete")
    @DeleteMapping("delete")
    public R<String> delete(@RequestParam(name = "id") Long id) {
        return R.successOnlyObject(oauthService.delete(id));
    }


    /**
     * @param sysOauth
     * @return
     */
    @NeedToken
    @PermissionCheck(role = "superadmin", permission = "sys:oauth:update")
    @PutMapping("update")
    public R<String> update(@RequestBody SysOauth sysOauth) {
        return R.successOnlyObject(oauthService.update(sysOauth));
    }

    @NeedToken
    @PermissionCheck(role = "superadmin", permission = "sys:oauth:add")
    @PostMapping("add")
    public R<String> add(@RequestBody SysOauth sysOauth) {
        return R.successOnlyObject(oauthService.add(sysOauth));
    }

}
