package com.qc.printers.custom.user.service;

import com.qc.printers.custom.user.domain.vo.response.app.AppState;

public interface UserFrontConfigService {
    AppState getUserFrontConfig();

    String setUserFrontConfig(AppState config);

}
