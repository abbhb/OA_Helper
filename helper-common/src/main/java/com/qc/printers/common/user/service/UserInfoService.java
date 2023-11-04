package com.qc.printers.common.user.service;

import com.qc.printers.common.user.domain.dto.UserInfo;
import com.qc.printers.common.user.domain.entity.SysRole;

import java.util.Set;

public interface UserInfoService {
    UserInfo getUserInfo(Long userId);

    Set<SysRole> getUserAllRole(Long userId);

}
