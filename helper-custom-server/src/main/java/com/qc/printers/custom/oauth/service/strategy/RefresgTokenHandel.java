package com.qc.printers.custom.oauth.service.strategy;

import com.qc.printers.common.common.MyString;
import com.qc.printers.common.common.utils.RedisUtils;
import com.qc.printers.custom.oauth.domain.dto.AccessToken;
import com.qc.printers.custom.oauth.domain.dto.Authorize;
import com.qc.printers.custom.oauth.domain.dto.RefreshToken;
import com.qc.printers.custom.oauth.domain.enums.AccessTokenEnum;
import com.qc.printers.custom.oauth.domain.vo.resp.TokenResp;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class RefresgTokenHandel extends GetAccessTokenHandel {
    @Override
    AccessTokenEnum getDataTypeEnum() {
        return AccessTokenEnum.REFRESHTOKEN;
    }

    @Override
    public TokenResp heXinChuLi(Authorize authorize) {
        TokenResp tokenResp = new TokenResp();
        Long expire = RedisUtils.getExpire(MyString.oauth_refresh_token + authorize.getRefreshToken(), TimeUnit.SECONDS);
        RefreshToken refreshTokenlast = RedisUtils.get(MyString.oauth_refresh_token + authorize.getRefreshToken(), RefreshToken.class);
        if (refreshTokenlast == null) {
            //已经过期
            tokenResp.setCode(100037);
            tokenResp.setMsg("RefreshToken已过期");
            return tokenResp;
        }
        UUID uuid = UUID.randomUUID();
        UUID uuid1 = UUID.randomUUID();
        String accessToken = uuid.toString();
        String refreshToken = uuid1.toString();
        if (refreshTokenlast.getUserId() == null) {
            tokenResp.setCode(100058);
            tokenResp.setMsg("非法接入");
            return tokenResp;
        }

        if (RedisUtils.get(MyString.oauth_access_token + refreshTokenlast.getAccessToken(), AccessToken.class) != null) {
            RedisUtils.del(MyString.oauth_access_token + refreshTokenlast.getAccessToken());
        }
        AccessToken accessToken1 = new AccessToken();
        accessToken1.setClientId(authorize.getClientId());
        accessToken1.setUserId(refreshTokenlast.getUserId());
        RedisUtils.set(MyString.oauth_access_token + accessToken, accessToken1, 2 * 3600L);
        RefreshToken refreshToken1 = new RefreshToken();
        refreshToken1.setAccessToken(accessToken);
        refreshToken1.setUserId(refreshTokenlast.getUserId());
        // 刷新token也只能让用一次,过期时间也得刷新，原令牌删除
        RedisUtils.del(MyString.oauth_refresh_token + authorize.getRefreshToken());
        RedisUtils.set(MyString.oauth_refresh_token + refreshToken, refreshToken1, expire);
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
        if (!authorize.getGrantType().equals("refresh_token")) {
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
        if (StringUtils.isEmpty(authorize.getClientSecret())) {
            tokenResp.setCode(100002);
            tokenResp.setMsg("缺少参数client_secret");
            return tokenResp;
        }
        if (StringUtils.isEmpty(authorize.getRefreshToken())) {
            tokenResp.setCode(100042);
            tokenResp.setMsg("RefreshToken非法");
            return tokenResp;
        }
        tokenResp.setCode(0);
        return tokenResp;
    }
}
