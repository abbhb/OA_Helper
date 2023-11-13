package com.qc.printers.custom.oauth.service.strategy;

import com.qc.printers.common.common.MyString;
import com.qc.printers.common.common.utils.RedisUtils;
import com.qc.printers.common.oauth.domain.dto.AccessToken;
import com.qc.printers.custom.oauth.domain.dto.Authorize;
import com.qc.printers.custom.oauth.domain.dto.OauthCodeDto;
import com.qc.printers.custom.oauth.domain.dto.RefreshToken;
import com.qc.printers.custom.oauth.domain.enums.AccessTokenEnum;
import com.qc.printers.custom.oauth.domain.vo.resp.TokenResp;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Slf4j
public class AccessTokenHandel extends GetAccessTokenHandel {
    @Override
    AccessTokenEnum getDataTypeEnum() {
        return AccessTokenEnum.ACCESSTOKEN;
    }

    @Override
    public TokenResp heXinChuLi(Authorize authorize) {
        TokenResp tokenResp = new TokenResp();
        UUID uuid = UUID.randomUUID();
        UUID uuid1 = UUID.randomUUID();
        String accessToken = uuid.toString();
        String refreshToken = uuid1.toString();
        OauthCodeDto oauthCodeDto = RedisUtils.get(MyString.oauth_code + authorize.getCode(), OauthCodeDto.class);
        if (oauthCodeDto == null) {
            tokenResp.setCode(100067);
            tokenResp.setMsg("code过期");
            return tokenResp;
        }
        RedisUtils.del(MyString.oauth_code + authorize.getCode());
        if (oauthCodeDto.getUserId() == null) {
            tokenResp.setCode(100058);
            tokenResp.setMsg("非法接入");
            return tokenResp;
        }
        if (!oauthCodeDto.getRedirectUri().equals(authorize.getRedirectUri())) {
            tokenResp.setCode(100019);
            tokenResp.setMsg("与获取code时给的回调地址不同");
            return tokenResp;
        }
        AccessToken accessToken1 = new AccessToken();
        accessToken1.setClientId(authorize.getClientId());
        accessToken1.setUserId(oauthCodeDto.getUserId());
        RedisUtils.set(MyString.oauth_access_token + accessToken, accessToken1, 2 * 3600L);
        RefreshToken refreshToken1 = new RefreshToken();
        refreshToken1.setAccessToken(accessToken);
        refreshToken1.setUserId(oauthCodeDto.getUserId());
        // 刷新token也只能让用一次
        RedisUtils.set(MyString.oauth_refresh_token + refreshToken, refreshToken1, 24 * 3600L);
        TokenResp tokenResp1 = new TokenResp();
        tokenResp1.setRefreshToken(refreshToken);
        tokenResp1.setAccessToken(accessToken);
        tokenResp1.setExpiresIn(2 * 3600L);
        tokenResp1.setCode(0);
        tokenResp1.setMsg("正常");
        return tokenResp1;
    }

    @Override
    public TokenResp canShuJiaoYan(Authorize authorize) {
        TokenResp tokenResp = new TokenResp();
        if (!authorize.getGrantType().equals("authorization_code")) {
            tokenResp.setCode(100000);
            tokenResp.setMsg("缺少参数response_type或response_type非法");
            return tokenResp;
        }
        if (authorize == null) {
            tokenResp.setCode(110505);
            tokenResp.setMsg("参数异常");
            return tokenResp;
        }
        if (StringUtils.isEmpty(authorize.getClientId())) {
            tokenResp.setCode(100001);
            tokenResp.setMsg("缺少参数client_id。");
            return tokenResp;
        }
        if (StringUtils.isEmpty(authorize.getCode())) {
            tokenResp.setCode(100005);
            tokenResp.setMsg("缺少参数code。");
            return tokenResp;
        }
        if (StringUtils.isEmpty(authorize.getClientSecret())) {
            tokenResp.setCode(100002);
            tokenResp.setMsg("缺少参数client_secret");
            return tokenResp;
        }
        if (StringUtils.isEmpty(authorize.getRedirectUri())) {
            tokenResp.setCode(100044);
            tokenResp.setMsg("缺少RedirectUri");
            return tokenResp;
        }


        tokenResp.setCode(0);
        return tokenResp;
    }
}
