package com.qc.printers.custom.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.qc.printers.common.common.Code;
import com.qc.printers.common.common.CustomException;
import com.qc.printers.common.common.MyString;
import com.qc.printers.common.common.constant.StringConstant;
import com.qc.printers.common.common.domain.entity.CommonConfig;
import com.qc.printers.common.common.service.CommonConfigService;
import com.qc.printers.common.common.utils.RedisUtils;
import com.qc.printers.common.common.utils.StringUtils;
import com.qc.printers.common.common.utils.ThreadLocalUtil;
import com.qc.printers.common.user.dao.UserFrontConfigDao;
import com.qc.printers.common.user.domain.dto.UserInfo;
import com.qc.printers.common.user.domain.entity.UserFrontConfig;
import com.qc.printers.custom.user.domain.vo.response.app.AppState;
import com.qc.printers.custom.user.service.UserFrontConfigService;
import jodd.io.StreamUtil;
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

    @Autowired
    private CommonConfigService commonConfigService;

    @Transactional
    @Override
    public AppState getUserFrontConfig() {
        UserInfo currentUser = ThreadLocalUtil.getCurrentUser();
        if (currentUser == null) {
            throw new CustomException("请先登录", Code.DEL_TOKEN);
        }
        AppState appState = new AppState();
        // 是否页脚
        CommonConfig frontFooter = commonConfigService.getById(StringConstant.FRONT_FOOTER);
        if (frontFooter != null) {
            appState.setFooter(Boolean.valueOf(frontFooter.getConfigValue()));
        }
        // 菜单宽度
        CommonConfig frontMenuWidth = commonConfigService.getById(StringConstant.FRONT_MENU_WIDTH);
        if (frontMenuWidth != null) {
            appState.setMenuWidth(Integer.valueOf(frontMenuWidth.getConfigValue()));
        }
        // model窗是否全屏
        CommonConfig frontModelFullscreen = commonConfigService.getById(StringConstant.FRONT_MODEL_FULLSCREEN);
        if (frontModelFullscreen != null) {
            appState.setModelFullscreen(Boolean.valueOf(frontModelFullscreen.getConfigValue()));
        }
        // 启用顶部菜单栏
        CommonConfig frontTopMenu = commonConfigService.getById(StringConstant.FRONT_MODEL_TOP_MENU);
        if (frontTopMenu != null) {
            appState.setTopMenu(Boolean.valueOf(frontTopMenu.getConfigValue()));
        }
        // 启用多页签
        CommonConfig frontTabBar = commonConfigService.getById(StringConstant.FRONT_TAB_BAR);
        if (frontTabBar != null) {
            appState.setTabBar(Boolean.valueOf(frontTabBar.getConfigValue()));
        }

        LambdaQueryWrapper<UserFrontConfig> userFrontConfigLambdaQueryWrapper = new LambdaQueryWrapper<>();
        userFrontConfigLambdaQueryWrapper.eq(UserFrontConfig::getUserId, currentUser.getId());
        int count = (int) userFrontConfigDao.count(userFrontConfigLambdaQueryWrapper);
        if (count == 0) {
            // 创建该用户默认的用户config
            UserFrontConfig userFrontConfig = new UserFrontConfig();
            userFrontConfig.setUserId(currentUser.getId());
            userFrontConfig.setTheme("light");
            userFrontConfig.setColorWeak(0);
            userFrontConfig.setLastPrintDevice("");
            userFrontConfig.setVersionRead("");
            userFrontConfigDao.save(userFrontConfig);
            return AppState.buildAppStateWithUserConfig(appState, userFrontConfig);
        }
        UserFrontConfig one = userFrontConfigDao.getOne(userFrontConfigLambdaQueryWrapper);
        if (one==null){
            throw new CustomException("异常的获取");
        }
        return AppState.buildAppStateWithUserConfig(appState, one);
    }

    @Transactional
    @Override
    public String setUserFrontConfig(AppState appState) {
        UserInfo currentUser = ThreadLocalUtil.getCurrentUser();
        if (currentUser == null) {
            throw new CustomException("请先登录", Code.DEL_TOKEN);
        }
        UserFrontConfig userFrontConfig1 = appState.buildUserFrontConfig(currentUser.getId());
        LambdaQueryWrapper<UserFrontConfig> userFrontConfigLambdaQueryWrapper = new LambdaQueryWrapper<>();
        userFrontConfigLambdaQueryWrapper.eq(UserFrontConfig::getUserId, currentUser.getId());
        UserFrontConfig one = userFrontConfigDao.getOne(userFrontConfigLambdaQueryWrapper);
        if (one!=null) {
            userFrontConfig1.setId(one.getId());
            userFrontConfig1.setUserId(one.userId);
            userFrontConfigDao.updateById(userFrontConfig1);
            return "更新成功";
        }
        userFrontConfig1.setUserId(currentUser.getId());
        userFrontConfigDao.save(userFrontConfig1);
        return "更新成功";
    }
}
