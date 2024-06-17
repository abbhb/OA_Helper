package com.qc.printers.common.chat.service;

import com.qc.printers.common.user.domain.enums.RoleEnum;

/**
 * 聊天室的角色
 */
public interface IRoleService {
    /**
     * 是否有某个权限，临时做法
     *
     * @return
     */
    boolean hasPower(Long uid, RoleEnum roleEnum);
}
