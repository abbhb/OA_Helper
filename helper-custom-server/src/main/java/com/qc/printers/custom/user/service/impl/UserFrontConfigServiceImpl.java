package com.qc.printers.custom.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.qc.printers.common.common.Code;
import com.qc.printers.common.common.CustomException;
import com.qc.printers.common.common.MyString;
import com.qc.printers.common.common.utils.RedisUtils;
import com.qc.printers.common.common.utils.ThreadLocalUtil;
import com.qc.printers.common.user.dao.UserFrontConfigDao;
import com.qc.printers.common.user.domain.dto.UserInfo;
import com.qc.printers.common.user.domain.entity.UserFrontConfig;
import com.qc.printers.custom.user.domain.dto.UserFrontConfigRedisDto;
import com.qc.printers.custom.user.service.UserFrontConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

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

        UserFrontConfigRedisDto userFrontConfigRedisDto = RedisUtils.get(MyString.user_front_key + currentUser.getId(), UserFrontConfigRedisDto.class);
        if (userFrontConfigRedisDto == null) {
            LambdaQueryWrapper<UserFrontConfig> userFrontConfigLambdaQueryWrapper = new LambdaQueryWrapper<>();
            userFrontConfigLambdaQueryWrapper.eq(UserFrontConfig::getUserId, currentUser.getId());
            int count = userFrontConfigDao.count(userFrontConfigLambdaQueryWrapper);
            if (count > 0) {
                UserFrontConfig userFrontConfig = userFrontConfigDao.getOne(userFrontConfigLambdaQueryWrapper);
                // 更新redis
                userFrontConfigRedisDto = new UserFrontConfigRedisDto();
                userFrontConfigRedisDto.setLastUpdate(LocalDateTime.now());
                userFrontConfigRedisDto.setUserId(currentUser.getId());
                userFrontConfigRedisDto.setConfigStr(userFrontConfig.getConfigStr());
                userFrontConfigRedisDto.setId(userFrontConfig.getId());
                userFrontConfigRedisDto.setCreateTime(userFrontConfig.getCreateTime());
                userFrontConfigRedisDto.setUpdateTime(userFrontConfig.getUpdateTime());
                return userFrontConfig.getConfigStr();
            }
            // 可能该用户是第一次使用
            return "{}";
        }
        return userFrontConfigRedisDto.getConfigStr();

    }

    @Transactional
    @Override
    public String setUserFrontConfig(String config) {
        UserInfo currentUser = ThreadLocalUtil.getCurrentUser();
        if (currentUser == null) {
            throw new CustomException("请先登录", Code.DEL_TOKEN);
        }
        if (config.equals("")) {
            config = "{}";
        }
        UserFrontConfigRedisDto userFrontConfigRedisDto = RedisUtils.get(MyString.user_front_key + currentUser.getId(), UserFrontConfigRedisDto.class);
        if (userFrontConfigRedisDto == null) {
            // redis也同步更新
            userFrontConfigRedisDto = new UserFrontConfigRedisDto();
            userFrontConfigRedisDto.setLastUpdate(LocalDateTime.now());
            userFrontConfigRedisDto.setUserId(currentUser.getId());
            userFrontConfigRedisDto.setConfigStr(config);
            RedisUtils.set(MyString.user_front_key + currentUser.getId(), userFrontConfigRedisDto);
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
        LocalDateTime dateTime = LocalDateTime.now();

        //是否是大于一分钟
        if (dateTime.isAfter(userFrontConfigRedisDto.getLastUpdate().plusMinutes(1))) {
            LambdaUpdateWrapper<UserFrontConfig> userFrontConfigLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
            userFrontConfigLambdaUpdateWrapper.eq(UserFrontConfig::getUserId, currentUser.getId());
            userFrontConfigLambdaUpdateWrapper.set(UserFrontConfig::getConfigStr, config);
            userFrontConfigDao.update(userFrontConfigLambdaUpdateWrapper);
            //只有写入数据库才重计量时间
            userFrontConfigRedisDto.setLastUpdate(dateTime);
        }
        userFrontConfigRedisDto.setConfigStr(config);
        RedisUtils.set(MyString.user_front_key + currentUser.getId(), userFrontConfigRedisDto);
        return "更新成功";
    }
}
