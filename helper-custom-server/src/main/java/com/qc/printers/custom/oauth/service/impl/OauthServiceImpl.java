package com.qc.printers.custom.oauth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qc.printers.common.common.Code;
import com.qc.printers.common.common.CustomException;
import com.qc.printers.common.common.MyString;
import com.qc.printers.common.common.utils.JWTUtil;
import com.qc.printers.common.common.utils.RedisUtils;
import com.qc.printers.common.common.utils.ThreadLocalUtil;
import com.qc.printers.common.common.utils.oss.OssDBUtil;
import com.qc.printers.common.oauth.annotation.CheckScope;
import com.qc.printers.common.oauth.dao.SysOauthDao;
import com.qc.printers.common.oauth.dao.SysOauthOpenidDao;
import com.qc.printers.common.oauth.dao.SysOauthUserDao;
import com.qc.printers.common.oauth.domain.dto.AccessToken;
import com.qc.printers.common.oauth.domain.entity.SysOauth;
import com.qc.printers.common.oauth.domain.entity.SysOauthOpenid;
import com.qc.printers.common.oauth.domain.entity.SysOauthUser;
import com.qc.printers.common.oauth.service.OauthMangerService;
import com.qc.printers.common.oauth.service.OauthOpenidService;
import com.qc.printers.common.oauth.utils.OauthUtil;
import com.qc.printers.common.user.dao.UserDao;
import com.qc.printers.common.user.domain.dto.UserInfo;
import com.qc.printers.common.user.domain.entity.User;
import com.qc.printers.custom.oauth.domain.dto.OauthCodeDto;
import com.qc.printers.custom.oauth.domain.vo.CanAuthorize;
import com.qc.printers.custom.oauth.domain.vo.req.AgreeLoginReq;
import com.qc.printers.custom.oauth.domain.vo.req.AgreeReq;
import com.qc.printers.custom.oauth.domain.vo.resp.AgreeLoginResp;
import com.qc.printers.custom.oauth.domain.vo.resp.AgreeResp;
import com.qc.printers.custom.oauth.domain.vo.resp.MeResp;
import com.qc.printers.custom.oauth.domain.vo.resp.OauthUserInfoResp;
import com.qc.printers.custom.oauth.service.OauthService;
import com.qc.printers.custom.user.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class OauthServiceImpl implements OauthService {

    @Autowired
    private SysOauthDao sysOauthDao;
    @Autowired
    private SysOauthOpenidDao sysOauthOpenidDao;

    @Autowired
    private SysOauthUserDao sysOauthUserDao;

    @Autowired
    private OauthMangerService oauthMangerService;

    @Autowired
    private UserDao userDao;

    @Autowired
    private UserService userService;

    @Autowired
    private OauthOpenidService oauthOpenidService;

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
                return new CanAuthorize(false, "回调地址不安全，请参考业务域名");
            }
        }
        return new CanAuthorize(true, "");
    }

    @Override
    public AgreeResp agree(AgreeReq agreeReq) {
        UserInfo currentUser = ThreadLocalUtil.getCurrentUser();
        if (currentUser == null) {
            throw new CustomException("请先登录", Code.DEL_TOKEN);
        }
        String code = OauthUtil.genCode();
        //20秒过期的code
        LambdaQueryWrapper<SysOauth> sysOauthLambdaQueryWrapper = new LambdaQueryWrapper<>();
        sysOauthLambdaQueryWrapper.eq(SysOauth::getClientId, agreeReq.getClientId());
        SysOauth sysOauth = sysOauthDao.getOne(sysOauthLambdaQueryWrapper);
        if (sysOauth == null) {
            throw new CustomException("业务异常");
        }
        if (sysOauth.getNoSertRedirect().equals(1)) {
            //判断回调地址是否合法
            String host1 = getIP(URI.create(agreeReq.getRedirectUri())).getHost();
            String host = getIP(URI.create(sysOauth.getDomainName())).getHost();
            if (!host.equals(host1)) {
                throw new CustomException("业务异常");
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
        boolean haveOpenidByUser = oauthOpenidService.isHaveOpenidByUser(currentUser.getId(), sysOauth.getId());
        if (!haveOpenidByUser) {
            Integer oneOpenidForOauth = oauthOpenidService.getOneOpenidForOauth(sysOauth.getId());
            SysOauthOpenid sysOauthOpenid = new SysOauthOpenid();
            sysOauthOpenid.setOpenid(oneOpenidForOauth);
            sysOauthOpenid.setSysOauthId(sysOauth.getId());
            sysOauthOpenid.setUserId(currentUser.getId());
            sysOauthOpenidDao.save(sysOauthOpenid);
        }
        oauthMangerService.userAgree(sysOauth.getId(), currentUser.getId(), agreeReq.getScope());
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
            throw new CustomException("业务异常");
        }
        if (sysOauth.getNoSertRedirect().equals(1)) {
            //判断回调地址是否合法
            String host1 = getIP(URI.create(agreeReq.getRedirectUri())).getHost();
            String host = getIP(URI.create(sysOauth.getDomainName())).getHost();
            if (!host.equals(host1)) {
                throw new CustomException("不安全的授权");
            }
        }


        User one = userService.loginPublic(agreeReq.getUsername(), agreeReq.getPassword());
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
        //授权
        oauthMangerService.userAgree(sysOauth.getId(), one.getId(), agreeReq.getScope());
        return agreeLoginResp;
    }

    @Override
    public MeResp me(String accessToken) {
        MeResp meResp = new MeResp();
        AccessToken accessToken1 = RedisUtils.get(MyString.oauth_access_token + accessToken, AccessToken.class);
        if (accessToken1.getUserId() != null) {
            LambdaQueryWrapper<User> sysUserLambdaQueryWrapper = new LambdaQueryWrapper<>();
            sysUserLambdaQueryWrapper.eq(User::getId, accessToken1.getUserId());
            User one = userDao.getOne(sysUserLambdaQueryWrapper);
            if (one == null) {
                meResp.setCode(100055);
                meResp.setMsg("查询用户授权信息失败");
                return meResp;
            }
            LambdaQueryWrapper<SysOauth> sysOauthLambdaQueryWrapper = new LambdaQueryWrapper<>();
            sysOauthLambdaQueryWrapper.eq(SysOauth::getClientId, accessToken1.getClientId());
            SysOauth sysOauth = sysOauthDao.getOne(sysOauthLambdaQueryWrapper);
            LambdaQueryWrapper<SysOauthOpenid> sysOauthOpenidLambdaQueryWrapper = new LambdaQueryWrapper<>();
            sysOauthOpenidLambdaQueryWrapper.eq(SysOauthOpenid::getSysOauthId, sysOauth.getId());
            SysOauthOpenid sysOauthOpenid = sysOauthOpenidDao.getOne(sysOauthOpenidLambdaQueryWrapper);
            meResp.setClientId(accessToken1.getClientId());
            meResp.setOpenId(String.valueOf(sysOauthOpenid.getOpenid()));
            meResp.setCode(0);
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



    @Override
    public List<SysOauth> list() {
        return oauthMangerService.listOauth();
    }

    @Transactional
    @Override
    public String delete(Long id) {
        if (id == null) {
            throw new CustomException("id不能为空");
        }
        return oauthMangerService.deleteOauth(id);
    }

    @Transactional
    @Override
    public String update(SysOauth sysOauth) {
        if (sysOauth.getId() == null) {
            throw new CustomException("id不能为空");
        }
        if (StringUtils.isEmpty(sysOauth.getClientId())) {
            throw new CustomException("clientId不能为空");
        }
        if (StringUtils.isEmpty(sysOauth.getClientSecret())) {
            throw new CustomException("clientSecret不能为空");
        }
        if (StringUtils.isEmpty(sysOauth.getClientName())) {
            throw new CustomException("clientName不能为空");
        }
        if (sysOauth.getForceConfigurationRedirect() == null) {
            throw new CustomException("请选择静态回调或动态回调");
        }
        if (sysOauth.getForceConfigurationRedirect().equals(1) && StringUtils.isEmpty(sysOauth.getRedirectUri())) {
            throw new CustomException("静态回调必须包含回调地址");
        }
        return oauthMangerService.updateOauth(sysOauth);
    }

    @Transactional
    @Override
    public String add(SysOauth sysOauth) {
        if (sysOauth.getForceConfigurationRedirect() == null) {
            throw new CustomException("请选择静态回调或动态回调");
        }
        if (sysOauth.getForceConfigurationRedirect().equals(1) && StringUtils.isEmpty(sysOauth.getRedirectUri())) {
            throw new CustomException("静态回调必须包含回调地址");
        }
        sysOauth.setId(null);
        return oauthMangerService.addOauth(sysOauth);
    }

    @Override
    public String getUserWithClientScope(Long userId, String clientId) {
        LambdaQueryWrapper<SysOauth> sysOauthLambdaQueryWrapper = new LambdaQueryWrapper<>();
        sysOauthLambdaQueryWrapper.eq(SysOauth::getClientId, clientId);
        SysOauth one = sysOauthDao.getOne(sysOauthLambdaQueryWrapper);
        LambdaQueryWrapper<SysOauthUser> sysOauthUserLambdaQueryWrapper = new LambdaQueryWrapper<>();
        sysOauthUserLambdaQueryWrapper.eq(SysOauthUser::getUserId, userId);
        sysOauthUserLambdaQueryWrapper.eq(SysOauthUser::getOauthId, one.getId());
        List<SysOauthUser> list = sysOauthUserDao.list(sysOauthUserLambdaQueryWrapper);
        if (list == null || list.size() == 0) {
            return "";
        }
        if (list.size() == 1) {
            return list.get(0).getScope();
        }
        String scope = new String();
        for (int i = 0; i < list.size(); i++) {
            scope = scope + list.get(i).getScope() + ",";
        }
        return scope.substring(0, scope.length() - 1);
    }

    @Override
    public OauthUserInfoResp getUserInfoHeader(HttpServletRequest request) {
        log.info("oauth_user_info:req:{}", request.getHeader(JWTUtil.AUTH_HEADER_KEY));
        final String authHeader = request.getHeader(JWTUtil.AUTH_HEADER_KEY);
        OauthUserInfoResp oauthUserInfoResp = new OauthUserInfoResp();
        if (StringUtils.isBlank(authHeader) || !authHeader.startsWith(JWTUtil.TOKEN_PREFIX)) {
            oauthUserInfoResp.setCode(10065);
            oauthUserInfoResp.setMsg("请先登录!");
            return oauthUserInfoResp;
        }
        // 获取token
        final String token = authHeader.substring(7);
        return this.getUserInfoOnlyAccessToken(token);
    }

    @Override
    public String getEndRedirectUri(String clientId, String redirectUri) {
        LambdaQueryWrapper<SysOauth> sysOauthLambdaQueryWrapper = new LambdaQueryWrapper<>();
        sysOauthLambdaQueryWrapper.eq(SysOauth::getClientId, clientId);
        SysOauth sysOauth = sysOauthDao.getOne(sysOauthLambdaQueryWrapper);
        if (sysOauth == null) {
            throw new CustomException("无法获取该oauth客户端");
        }
        if (sysOauth.getForceConfigurationRedirect().equals(1)) {
            // 静态回调
            if (StringUtils.isEmpty(sysOauth.getRedirectUri())) {
                throw new CustomException("请检查oauth后台信息是否包含回调地址");
            }
            return sysOauth.getRedirectUri();
        }
        // 万一动态回调但是没传入回调地址就取数据库的
        if (StringUtils.isEmpty(redirectUri)) {
            return sysOauth.getRedirectUri();
        }
        return redirectUri;
    }

    @Override
    public String loginOut(HttpServletRequest request, Model model) {
        final String authHeader = request.getHeader(JWTUtil.AUTH_HEADER_KEY);
        OauthUserInfoResp oauthUserInfoResp = new OauthUserInfoResp();
        if (StringUtils.isBlank(authHeader) || !authHeader.startsWith(JWTUtil.TOKEN_PREFIX)) {
            model.addAttribute("error", "请先登录");
            return "error";
        }
        // 获取token
        final String token = authHeader.substring(7);
        if (StringUtils.isEmpty(token)) {
            model.addAttribute("error", "请先登录");
            return "error";
        }
        if (RedisUtils.hasKey(MyString.oauth_access_token + token)) {
            RedisUtils.del(MyString.oauth_access_token + token);
        }

        return "logoutSuccess";
    }

    /**
     * 校验权限
     *
     * @param accessToken
     * @param cilentId
     * @return
     */
    @CheckScope(token = "#accessToken", cliendId = "#cilentId", needScope = "get_user_info")
    @Override
    public OauthUserInfoResp getUserInfo(String accessToken, String cilentId) {
        OauthUserInfoResp oauthUserInfoResp = new OauthUserInfoResp();
        try {
            if (StringUtils.isEmpty(accessToken) || StringUtils.isEmpty(cilentId)) {
                log.info("getUserInfo参数{},{}", accessToken, cilentId);
                oauthUserInfoResp.setCode(100055);
                oauthUserInfoResp.setMsg("参数异常");
                return oauthUserInfoResp;
            }
            AccessToken accessTokenObject = RedisUtils.get(MyString.oauth_access_token + accessToken, AccessToken.class);
            if (accessTokenObject == null) {
                oauthUserInfoResp.setCode(100055);
                oauthUserInfoResp.setMsg("token失效");
                return oauthUserInfoResp;
            }
            if (accessTokenObject.getClientId().equals(cilentId)) {
                User one = userDao.getById(accessTokenObject.getUserId());
                if (one == null) {
                    oauthUserInfoResp.setCode(100055);
                    oauthUserInfoResp.setMsg("查询用户信息失败");
                    return oauthUserInfoResp;
                }
                // 获取openid 👇
                oauthUserInfoResp.setSex(one.getSex());
                oauthUserInfoResp.setEmail(one.getEmail());
                oauthUserInfoResp.setAvatar(OssDBUtil.toUseUrl(one.getAvatar()));
                oauthUserInfoResp.setNickname(one.getName());
                oauthUserInfoResp.setUsername(one.getUsername());
                oauthUserInfoResp.setName(one.getName());
                LambdaQueryWrapper<SysOauthOpenid> sysOauthOpenidLambdaQueryWrapper = new LambdaQueryWrapper<>();
                sysOauthOpenidLambdaQueryWrapper.eq(SysOauthOpenid::getUserId, one.getId());
                LambdaQueryWrapper<SysOauth> sysOauthLambdaQueryWrapper = new LambdaQueryWrapper<>();
                sysOauthLambdaQueryWrapper.eq(SysOauth::getClientId, cilentId);
                SysOauth sysOauthDaoOne = sysOauthDao.getOne(sysOauthLambdaQueryWrapper);
                sysOauthOpenidLambdaQueryWrapper.eq(SysOauthOpenid::getSysOauthId, sysOauthDaoOne.getId());
                SysOauthOpenid sysOauthOpenid = sysOauthOpenidDao.getOne(sysOauthOpenidLambdaQueryWrapper);
                // 获取openid 向👆

                oauthUserInfoResp.setId(sysOauthOpenid.getOpenid());
                oauthUserInfoResp.setCode(0);
                return oauthUserInfoResp;

            }
        } catch (Exception e) {
            log.error(e.getMessage());
            oauthUserInfoResp.setCode(100055);
            oauthUserInfoResp.setMsg("业务异常");
            return oauthUserInfoResp;
        }
        oauthUserInfoResp.setCode(100055);
        oauthUserInfoResp.setMsg("业务异常");
        return oauthUserInfoResp;
    }

    @Override
    public OauthUserInfoResp getUserInfoOnlyAccessToken(String accessToken) {
        OauthUserInfoResp oauthUserInfoResp = new OauthUserInfoResp();
        if (StringUtils.isEmpty(accessToken)) {
            oauthUserInfoResp.setCode(10065);
            oauthUserInfoResp.setMsg("请先登录!");
            return oauthUserInfoResp;
        }
        AccessToken accessTokenObject = RedisUtils.get(MyString.oauth_access_token + accessToken, AccessToken.class);
        if (accessTokenObject == null) {
            oauthUserInfoResp.setCode(10065);
            oauthUserInfoResp.setMsg("请先登录!");
            return oauthUserInfoResp;
        }
        Long userId = accessTokenObject.getUserId();
        String clientId = accessTokenObject.getClientId();
        if (userId == null) {
            oauthUserInfoResp.setCode(10065);
            oauthUserInfoResp.setMsg("请先登录!");
            return oauthUserInfoResp;
        }

        return this.getUserInfo(accessToken, clientId);

    }

    @Override
    public OauthUserInfoResp getUserInfoNeedCheckOpenId(String accessToken, String openid, String clientId) {
        OauthUserInfoResp oauthUserInfoResp = new OauthUserInfoResp();
        if (StringUtils.isEmpty(accessToken)) {
            oauthUserInfoResp.setCode(10065);
            oauthUserInfoResp.setMsg("请先登录!");
            return oauthUserInfoResp;
        }
        AccessToken accessTokenObject = RedisUtils.get(MyString.oauth_access_token + accessToken, AccessToken.class);
        if (accessTokenObject == null) {
            oauthUserInfoResp.setCode(10065);
            oauthUserInfoResp.setMsg("请先登录!");
            return oauthUserInfoResp;
        }
        Long userId = accessTokenObject.getUserId();
        String clientIdT = accessTokenObject.getClientId();
        if (userId == null) {
            oauthUserInfoResp.setCode(10065);
            oauthUserInfoResp.setMsg("请先登录!");
            return oauthUserInfoResp;
        }
        LambdaQueryWrapper<SysOauth> sysOauthLambdaQueryWrapper = new LambdaQueryWrapper<>();
        sysOauthLambdaQueryWrapper.eq(SysOauth::getClientId, clientIdT);
        SysOauth sysOauth = sysOauthDao.getOne(sysOauthLambdaQueryWrapper);
        LambdaQueryWrapper<SysOauthOpenid> sysOauthOpenidLambdaQueryWrapper = new LambdaQueryWrapper<>();
        sysOauthOpenidLambdaQueryWrapper.eq(SysOauthOpenid::getSysOauthId, sysOauth.getId());
        SysOauthOpenid sysOauthOpenid = sysOauthOpenidDao.getOne(sysOauthOpenidLambdaQueryWrapper);
        if (!sysOauthOpenid.getOpenid().equals(Integer.valueOf(openid))) {
            oauthUserInfoResp.setCode(10065);
            oauthUserInfoResp.setMsg("openid异常!");
            return oauthUserInfoResp;
        }
        return this.getUserInfo(accessToken, clientIdT);
    }

}
