package com.qc.printers.common.user.service;

import com.qc.printers.common.user.domain.dto.UserInfo;
import com.qc.printers.common.user.domain.entity.SysRole;
import com.qc.printers.common.user.domain.entity.User;

import java.util.Set;

public interface UserInfoService {
    UserInfo getUserInfo(Long userId);

    /**
     * 并不像写此接口方法，但是为了避免循环依赖
     * @param userId
     * @return
     */
    User getUserForCache(Long userId);

    Set<SysRole> getUserAllRole(Long userId);

}
