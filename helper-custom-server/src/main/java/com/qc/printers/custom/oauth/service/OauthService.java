package com.qc.printers.custom.oauth.service;

import com.qc.printers.common.oauth.domain.entity.SysOauth;
import com.qc.printers.custom.oauth.domain.vo.CanAuthorize;
import com.qc.printers.custom.oauth.domain.vo.req.AgreeLoginReq;
import com.qc.printers.custom.oauth.domain.vo.req.AgreeReq;
import com.qc.printers.custom.oauth.domain.vo.resp.AgreeLoginResp;
import com.qc.printers.custom.oauth.domain.vo.resp.AgreeResp;
import com.qc.printers.custom.oauth.domain.vo.resp.MeResp;
import com.qc.printers.custom.oauth.domain.vo.resp.OauthUserInfoResp;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

public interface OauthService {
    CanAuthorize isCanAuthorize(String responseType, String clientId, String redirectUri, String state, String scope);

    AgreeResp agree(AgreeReq agreeReq);

    AgreeLoginResp agreeLogin(AgreeLoginReq agreeReq);


    MeResp me(String accessToken);

    String getClientName(String clientId);

    OauthUserInfoResp getUserInfo(String accessToken, String openid, String cilentId);

    List<SysOauth> list();

    String delete(Long id);

    String update(SysOauth sysOauth);

    String add(SysOauth sysOauth);

    OauthUserInfoResp getUserInfoHeader(HttpServletRequest request);
}
