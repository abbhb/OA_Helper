package com.qc.printers.custom.user.service;

import com.qc.printers.common.user.domain.dto.UserInfo;
import com.qc.printers.custom.user.domain.vo.response.MenuResult;

import java.util.List;

public interface MenuService {
    List<MenuResult> getUserMenu(UserInfo userInfo);
}
