package com.qc.printers.common.webauthn.dao;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qc.printers.common.user.domain.entity.User;
import com.qc.printers.common.webauthn.entity.UserPasskey;
import com.qc.printers.common.webauthn.mapper.UserPasskeyMapper;
import com.yubico.webauthn.data.ByteArray;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserPasskeyDao extends ServiceImpl<UserPasskeyMapper, UserPasskey> {
    public UserPasskey findByUserId(Long userId) {
        LambdaQueryWrapper<UserPasskey> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserPasskey::getUserId, userId);
        return getOne(queryWrapper);
    }
    public List<UserPasskey> fundAllByUser(User user) {
        LambdaQueryWrapper<UserPasskey> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserPasskey::getUserId, user.getId());
        return list(queryWrapper);
    }
    public List<UserPasskey> findAllByCredentialId(String credentialId) {
        LambdaQueryWrapper<UserPasskey> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserPasskey::getCredentialId, credentialId);
        return list(queryWrapper);
    }
    
    
}
