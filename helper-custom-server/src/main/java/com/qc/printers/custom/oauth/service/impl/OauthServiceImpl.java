package com.qc.printers.custom.oauth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qc.printers.common.common.CustomException;
import com.qc.printers.common.common.MyString;
import com.qc.printers.common.common.utils.JWTUtil;
import com.qc.printers.common.common.utils.PWDMD5;
import com.qc.printers.common.common.utils.RedisUtils;
import com.qc.printers.common.common.utils.ThreadLocalUtil;
import com.qc.printers.common.oauth.dao.SysOauthDao;
import com.qc.printers.common.oauth.domain.entity.SysOauth;
import com.qc.printers.common.oauth.domain.enums.OauthErrorEnum;
import com.qc.printers.common.oauth.exception.OauthException;
import com.qc.printers.common.oauth.service.OauthMangerService;
import com.qc.printers.common.oauth.utils.OauthUtil;
import com.qc.printers.common.user.dao.UserDao;
import com.qc.printers.common.user.domain.dto.UserInfo;
import com.qc.printers.common.user.domain.entity.User;
import com.qc.printers.custom.oauth.domain.dto.AccessToken;
import com.qc.printers.custom.oauth.domain.dto.OauthCodeDto;
import com.qc.printers.custom.oauth.domain.vo.CanAuthorize;
import com.qc.printers.custom.oauth.domain.vo.req.AgreeLoginReq;
import com.qc.printers.custom.oauth.domain.vo.req.AgreeReq;
import com.qc.printers.custom.oauth.domain.vo.resp.AgreeLoginResp;
import com.qc.printers.custom.oauth.domain.vo.resp.AgreeResp;
import com.qc.printers.custom.oauth.domain.vo.resp.MeResp;
import com.qc.printers.custom.oauth.service.OauthService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class OauthServiceImpl implements OauthService {

    @Autowired
    private SysOauthDao sysOauthDao;

    @Autowired
    private OauthMangerService oauthMangerService;

    @Autowired
    private UserDao userDao;

    private static URI getIP(URI uri) {
        URI effectiveURI = null;

        try {
            // URI(String scheme, String userInfo, String host, int port, String
            // path, String query,String fragment)
            effectiveURI = new URI(uri.getScheme(), uri.getUserInfo(), uri.getHost(), uri.getPort(), null, null, null);
        } catch (Throwable var4) {
            effectiveURI = null;
        }

        return effectiveURI;
    }

    @Override
    public CanAuthorize isCanAuthorize(String responseType, String clientId, String redirectUri, String state, String scope) {
        if (!responseType.equals("code")) {
            return new CanAuthorize(false, "oauth2IsMustCode");
        }
        LambdaQueryWrapper<SysOauth> sysOauthLambdaQueryWrapper = new LambdaQueryWrapper<>();
        sysOauthLambdaQueryWrapper.eq(SysOauth::getClientId, clientId);
        SysOauth sysOauth = sysOauthDao.getOne(sysOauthLambdaQueryWrapper);
        if (sysOauth == null) {
            return new CanAuthorize(false, "UnknownClient");
        }
        if (sysOauth.getNoSertRedirect().equals(1)) {
            //判断回调地址是否合法
            String host1 = getIP(URI.create(redirectUri)).getHost();
            String host = getIP(URI.create(sysOauth.getDomainName())).getHost();
            if (!host.equals(host1)) {
                return new CanAuthorize(false, "TheCallbackAddressIsNotSecure");
            }
        }
        return new CanAuthorize(true, "");
    }

    @Override
    public AgreeResp agree(AgreeReq agreeReq) {
        UserInfo currentUser = ThreadLocalUtil.getCurrentUser();
        if (currentUser == null) {
            throw new OauthException(OauthErrorEnum.OAUTH_NEED_AUTHORIZATION);
        }
        String code = OauthUtil.genCode();
        //20秒过期的code
        LambdaQueryWrapper<SysOauth> sysOauthLambdaQueryWrapper = new LambdaQueryWrapper<>();
        sysOauthLambdaQueryWrapper.eq(SysOauth::getClientId, agreeReq.getClientId());
        SysOauth sysOauth = sysOauthDao.getOne(sysOauthLambdaQueryWrapper);
        if (sysOauth == null) {
            throw new OauthException(OauthErrorEnum.OAUTH_ERROR, "TheCallbackAddressIsNotSecure");
        }
        if (sysOauth.getNoSertRedirect().equals(1)) {
            //判断回调地址是否合法
            String host1 = getIP(URI.create(agreeReq.getRedirectUri())).getHost();
            String host = getIP(URI.create(sysOauth.getDomainName())).getHost();
            if (!host.equals(host1)) {
                throw new OauthException(OauthErrorEnum.OAUTH_ERROR, "TheCallbackAddressIsNotSecure");
            }
        }
        OauthCodeDto oauthCodeDto = new OauthCodeDto();
        oauthCodeDto.setRedirectUri(agreeReq.getRedirectUri());
        oauthCodeDto.setUserId(currentUser.getId());
        RedisUtils.set(MyString.oauth_code + code, oauthCodeDto, 20L, TimeUnit.SECONDS);
        AgreeResp agreeResp = new AgreeResp();
        agreeResp.setCode(code);
        agreeResp.setState(agreeReq.getState());
        agreeResp.setRedirectUri(agreeReq.getRedirectUri());
        return agreeResp;
    }

    @Override
    public AgreeLoginResp agreeLogin(AgreeLoginReq agreeReq) {

        if (StringUtils.isEmpty(agreeReq.getUsername()) || StringUtils.isEmpty(agreeReq.getPassword())) {
            throw new CustomException("参数异常");
        }
        LambdaQueryWrapper<SysOauth> sysOauthLambdaQueryWrapper = new LambdaQueryWrapper<>();
        sysOauthLambdaQueryWrapper.eq(SysOauth::getClientId, agreeReq.getClientId());
        SysOauth sysOauth = sysOauthDao.getOne(sysOauthLambdaQueryWrapper);
        if (sysOauth == null) {
            throw new OauthException(OauthErrorEnum.OAUTH_ERROR, "TheCallbackAddressIsNotSecure");
        }
        if (sysOauth.getNoSertRedirect().equals(1)) {
            //判断回调地址是否合法
            String host1 = getIP(URI.create(agreeReq.getRedirectUri())).getHost();
            String host = getIP(URI.create(sysOauth.getDomainName())).getHost();
            if (!host.equals(host1)) {
                throw new OauthException(OauthErrorEnum.OAUTH_ERROR, "不安全的授权");
            }
        }

        LambdaQueryWrapper<User> userLambdaQueryWrapper = new LambdaQueryWrapper<>();
        userLambdaQueryWrapper.eq(User::getUsername, agreeReq.getUsername());
        User one = userDao.getOne(userLambdaQueryWrapper);
        if (one == null) {
            throw new CustomException("用户名密码错误");
        }
        if (one.getPassword() == null || one.getSalt() == null) {
            throw new CustomException("此用户未设置密码，请联系管理员");
        }
        try {
            String password = PWDMD5.getMD5Encryption(agreeReq.getPassword(), one.getSalt());
            if (!one.getPassword().equals(password)) {
                throw new CustomException("用户名密码错误");
            }
        } catch (Exception e) {
            throw new CustomException("刷新再试试");
        }
        if (one.getStatus().equals(0)) {
            throw new CustomException("账号已被禁用");
        }
        //同时返回token和code，顺便给网站一个token
        String token = JWTUtil.getToken(String.valueOf(one.getId()));


        RedisUtils.set(token, String.valueOf(one.getId()), 12 * 3600L, TimeUnit.SECONDS);


        String code = OauthUtil.genCode();
        //20秒过期的code
        OauthCodeDto oauthCodeDto = new OauthCodeDto();
        oauthCodeDto.setRedirectUri(agreeReq.getRedirectUri());
        oauthCodeDto.setUserId(one.getId());
        RedisUtils.set(MyString.oauth_code + code, oauthCodeDto, 20L, TimeUnit.SECONDS);
        AgreeLoginResp agreeLoginResp = new AgreeLoginResp();
        agreeLoginResp.setCode(code);
        agreeLoginResp.setState(agreeReq.getState());
        agreeLoginResp.setToken(token);
        agreeLoginResp.setRedirectUri(agreeReq.getRedirectUri());
        return agreeLoginResp;
    }

    @Override
    public MeResp me(String accessToken) {
        MeResp meResp = new MeResp();
        AccessToken accessToken1 = RedisUtils.get(MyString.oauth_access_token + accessToken, AccessToken.class);
        if (accessToken1.getUserId() != null) {
            LambdaQueryWrapper<User> sysOauthLambdaQueryWrapper = new LambdaQueryWrapper<>();
            sysOauthLambdaQueryWrapper.eq(User::getId, accessToken1.getUserId());
            User one = userDao.getOne(sysOauthLambdaQueryWrapper);
            if (one == null) {
                meResp.setCode(100055);
                meResp.setMsg("查询用户授权信息失败");
                return meResp;
            }
            meResp.setClientId(accessToken1.getClientId());
            meResp.setOpenId(one.getOpenId());
            return meResp;
        }
        meResp.setCode(100055);
        meResp.setMsg("查询用户授权信息失败");
        return meResp;
    }

    @Override
    public String getClientName(String clientId) {
        LambdaQueryWrapper<SysOauth> sysOauthLambdaQueryWrapper = new LambdaQueryWrapper<>();
        sysOauthLambdaQueryWrapper.eq(SysOauth::getClientId, clientId);
        SysOauth one = sysOauthDao.getOne(sysOauthLambdaQueryWrapper);
        return one.getClientName();
    }

}
