package com.qc.printers.custom.user.domain.vo.response.app;

import cn.hutool.core.bean.BeanUtil;

import com.qc.printers.common.user.domain.entity.UserFrontConfig;
import lombok.Data;
import java.io.Serializable;


@Data
public class AppState implements Serializable {

    // 全局配置

    private Integer menuWidth = 220;

    private Boolean tabBar = false;

    private Boolean topMenu = true;

    private Boolean footer = true;

    private Boolean modelFullscreen = false;

    //用户个人配置

    private String theme;

    private Boolean colorWeak;

    private String lastPrintDevice;

    private String versionRead;

    public static AppState buildAppStateWithUserConfig(AppState appState, UserFrontConfig userFrontConfig) {
        AppState appState1 = new AppState();
        BeanUtil.copyProperties(appState, appState1);
        appState1.setTheme(userFrontConfig.getTheme());
        appState1.setVersionRead(userFrontConfig.getVersionRead());
        appState1.setColorWeak(!userFrontConfig.getColorWeak().equals(0));
        appState1.setLastPrintDevice(userFrontConfig.getLastPrintDevice());
        return appState1;
    }

    public UserFrontConfig buildUserFrontConfig(Long userId) {
        UserFrontConfig userFrontConfig = new UserFrontConfig();
        userFrontConfig.setUserId(userId);
        userFrontConfig.setTheme(theme);
        userFrontConfig.setColorWeak(colorWeak?1:0);
        userFrontConfig.setVersionRead(versionRead);
        userFrontConfig.setLastPrintDevice(lastPrintDevice);
        return userFrontConfig;
    }
}