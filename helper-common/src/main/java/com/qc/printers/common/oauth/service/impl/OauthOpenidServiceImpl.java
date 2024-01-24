package com.qc.printers.common.oauth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qc.printers.common.common.CustomException;
import com.qc.printers.common.common.MyString;
import com.qc.printers.common.common.annotation.RedissonLock;
import com.qc.printers.common.common.utils.RedisUtils;
import com.qc.printers.common.oauth.dao.SysOauthDao;
import com.qc.printers.common.oauth.dao.SysOauthOpenidDao;
import com.qc.printers.common.oauth.domain.entity.SysOauthOpenid;
import com.qc.printers.common.oauth.service.OauthOpenidService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class OauthOpenidServiceImpl implements OauthOpenidService {

    @Autowired
    private SysOauthOpenidDao sysOauthOpenidDao;

    @Autowired
    private SysOauthDao sysOauthDao;

    @RedissonLock(prefixKey = "Oauth_client_openid", key = "#oauthId", waitTime = 20, unit = TimeUnit.SECONDS)
    @Override
    public Integer getOneOpenidForOauth(Long oauthId) {
        if (!RedisUtils.hasKey(MyString.oauth_client_inr + oauthId)) {
            initRedisOpenidMaxByClientId(oauthId);
        }
        Integer cishu = 1;
        while (cishu <= 3) {
            Long inc = RedisUtils.inc(MyString.oauth_client_inr + oauthId);
            LambdaQueryWrapper<SysOauthOpenid> sysOauthOpenidLambdaQueryWrapper = new LambdaQueryWrapper<>();
            sysOauthOpenidLambdaQueryWrapper.eq(SysOauthOpenid::getOpenid, inc.intValue());
            sysOauthOpenidLambdaQueryWrapper.eq(SysOauthOpenid::getSysOauthId, oauthId);
            int count = sysOauthOpenidDao.count(sysOauthOpenidLambdaQueryWrapper);
            if (count > 0) {
                cishu++;
                continue;
            }
            return inc.intValue();
        }
        throw new CustomException("无法获取openid");

    }


    @Override
    public boolean isHaveOpenid(Integer openid, Long oauthId) {
        LambdaQueryWrapper<SysOauthOpenid> sysOauthOpenidLambdaQueryWrapper = new LambdaQueryWrapper<>();
        sysOauthOpenidLambdaQueryWrapper.eq(SysOauthOpenid::getOpenid, openid);
        sysOauthOpenidLambdaQueryWrapper.eq(SysOauthOpenid::getSysOauthId, oauthId);
        int count = sysOauthOpenidDao.count(sysOauthOpenidLambdaQueryWrapper);
        return count > 0;
    }

    @Override
    public boolean isHaveOpenidByUser(Long userId, Long oauthId) {
        LambdaQueryWrapper<SysOauthOpenid> sysOauthOpenidLambdaQueryWrapper = new LambdaQueryWrapper<>();
        sysOauthOpenidLambdaQueryWrapper.eq(SysOauthOpenid::getUserId, userId);
        sysOauthOpenidLambdaQueryWrapper.eq(SysOauthOpenid::getSysOauthId, oauthId);
        int count = sysOauthOpenidDao.count(sysOauthOpenidLambdaQueryWrapper);
        return count > 0;
    }

    @RedissonLock(prefixKey = "Oauth_client_openid", key = "#oauthId", waitTime = 30, unit = TimeUnit.SECONDS)
    @Override
    public void initRedisOpenidMax(Long oauthId) {
        initRedisOpenidMaxByClientId(oauthId);
    }

    private void initRedisOpenidMaxByClientId(Long oauthId) {
        // 查出当前该oauth已有的
        LambdaQueryWrapper<SysOauthOpenid> sysOauthOpenidLambdaQueryWrapper = new LambdaQueryWrapper<>();
        sysOauthOpenidLambdaQueryWrapper.select(SysOauthOpenid::getOpenid);
        sysOauthOpenidLambdaQueryWrapper.eq(SysOauthOpenid::getSysOauthId, oauthId);
        sysOauthOpenidLambdaQueryWrapper.orderByDesc(SysOauthOpenid::getOpenid);
        List<SysOauthOpenid> list = sysOauthOpenidDao.list(sysOauthOpenidLambdaQueryWrapper);
        if (list == null || list.size() == 0) {
            RedisUtils.set(MyString.oauth_client_inr + oauthId, 0);
        } else {
            RedisUtils.set(MyString.oauth_client_inr + oauthId, list.get(0).getOpenid());
        }
    }
}
