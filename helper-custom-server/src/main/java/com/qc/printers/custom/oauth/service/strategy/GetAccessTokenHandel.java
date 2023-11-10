package com.qc.printers.custom.oauth.service.strategy;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qc.printers.common.oauth.dao.SysOauthDao;
import com.qc.printers.common.oauth.domain.entity.SysOauth;
import com.qc.printers.custom.oauth.domain.dto.Authorize;
import com.qc.printers.custom.oauth.domain.enums.AccessTokenEnum;
import com.qc.printers.custom.oauth.domain.vo.resp.TokenResp;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 获取access_token处理类
 */

@Service
@Slf4j
public abstract class GetAccessTokenHandel {

    @Autowired
    private SysOauthDao sysOauthDao;

    /**
     * 数据的类型
     */
    abstract AccessTokenEnum getDataTypeEnum();

    /**
     * 生成返回数据
     * 返回数据都基于PrinterBaseResp<T></>
     */
    public abstract TokenResp heXinChuLi(Authorize authorize);

    public abstract TokenResp canShuJiaoYan(Authorize authorize);

    public TokenResp getAccessToken(Authorize authorize) {
        TokenResp tokenResp2 = canShuJiaoYan(authorize);
        if (tokenResp2.getCode() != 0) {
            return tokenResp2;
        }
        TokenResp tokenResp = new TokenResp();
        LambdaQueryWrapper<SysOauth> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(SysOauth::getClientId, authorize.getClientId());
        SysOauth sysOauthDaoOne = sysOauthDao.getOne(lambdaQueryWrapper);
        if (sysOauthDaoOne == null) {
            tokenResp.setCode(100002);
            tokenResp.setMsg("缺少参数client_secret");
            return tokenResp;
        }
        if (!sysOauthDaoOne.getClientSecret().equals(authorize.getClientSecret())) {
            tokenResp.setCode(100002);
            tokenResp.setMsg("缺少参数client_secret");
            return tokenResp;
        }
        tokenResp = heXinChuLi(authorize);
        return tokenResp;
    }
}
