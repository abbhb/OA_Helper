package com.qc.printers.common.user.service;

import com.qc.printers.common.user.domain.dto.UserInfo;
import com.qc.printers.common.user.domain.entity.SysRole;

import java.util.Set;

public interface IUserService {

    UserInfo getUserInfo(Long userId);

    boolean isSuperAdmin(Set<SysRole> roleSet, Long userId);

}
