package com.qc.printers.custom.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.qc.printers.common.common.Code;
import com.qc.printers.common.common.CustomException;
import com.qc.printers.common.common.utils.ThreadLocalUtil;
import com.qc.printers.common.user.dao.UserFrontConfigDao;
import com.qc.printers.common.user.domain.dto.UserInfo;
import com.qc.printers.common.user.domain.entity.UserFrontConfig;
import com.qc.printers.custom.user.service.UserFrontConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class UserFrontConfigServiceImpl implements UserFrontConfigService {
    @Autowired
    private UserFrontConfigDao userFrontConfigDao;

    @Override
    public String getUserFrontConfig() {
        UserInfo currentUser = ThreadLocalUtil.getCurrentUser();
        if (currentUser == null) {
            throw new CustomException("请先登录", Code.DEL_TOKEN);
        }
        LambdaQueryWrapper<UserFrontConfig> userFrontConfigLambdaQueryWrapper = new LambdaQueryWrapper<>();
        userFrontConfigLambdaQueryWrapper.eq(UserFrontConfig::getUserId, currentUser.getId());
        int count = userFrontConfigDao.count(userFrontConfigLambdaQueryWrapper);
        if (count > 0) {
            return userFrontConfigDao.getOne(userFrontConfigLambdaQueryWrapper).getConfigStr();
        }
        // 可能该用户是第一次使用
        return "[]";
    }

    @Transactional
    @Override
    public String setUserFrontConfig(String config) {
        UserInfo currentUser = ThreadLocalUtil.getCurrentUser();
        if (currentUser == null) {
            throw new CustomException("请先登录", Code.DEL_TOKEN);
        }
        LambdaQueryWrapper<UserFrontConfig> userFrontConfigLambdaQueryWrapper = new LambdaQueryWrapper<>();
        userFrontConfigLambdaQueryWrapper.eq(UserFrontConfig::getUserId, currentUser.getId());
        int count = userFrontConfigDao.count(userFrontConfigLambdaQueryWrapper);
        if (count > 0) {
            //更新
            LambdaUpdateWrapper<UserFrontConfig> userFrontConfigLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
            userFrontConfigLambdaUpdateWrapper.eq(UserFrontConfig::getUserId, currentUser.getId());
            userFrontConfigLambdaUpdateWrapper.set(UserFrontConfig::getConfigStr, config);
            userFrontConfigDao.update(userFrontConfigLambdaUpdateWrapper);
            return "更新成功";
        }
        UserFrontConfig userFrontConfig = new UserFrontConfig();
        userFrontConfig.setUserId(currentUser.getId());
        userFrontConfig.setConfigStr(config);
        userFrontConfigDao.save(userFrontConfig);
        //否则就新增
        return "更新成功";
    }
}
