package com.qc.printers.custom.webauthn.service;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import cn.hutool.core.util.ByteUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qc.printers.common.user.dao.UserDao;
import com.qc.printers.common.user.domain.entity.User;
import com.qc.printers.common.webauthn.dao.UserPasskeyDao;
import com.qc.printers.common.webauthn.entity.UserPasskey;
import com.yubico.webauthn.CredentialRepository;
import com.yubico.webauthn.RegisteredCredential;
import com.yubico.webauthn.data.ByteArray;
import com.yubico.webauthn.data.PublicKeyCredentialDescriptor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import lombok.Getter;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@Getter
public class RegistrationService implements CredentialRepository  {
    @Autowired
    private UserDao userDao;

    @Autowired
    private UserPasskeyDao userPasskeyDao;


    @Override
    public Set<PublicKeyCredentialDescriptor> getCredentialIdsForUsername(String username) {
        LambdaQueryWrapper<User> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(User::getUsername,username);
        User user = userDao.getOne(lambdaQueryWrapper);
        List<UserPasskey> auth = userPasskeyDao.fundAllByUser(user);
        return auth.stream()
                .map(
                        credential ->
                                PublicKeyCredentialDescriptor.builder()
                                        .id(ByteArray.fromBase64(credential.getCredentialId()))
                                        .build())
                .collect(Collectors.toSet());
    }

    @Override
    public Optional<ByteArray> getUserHandleForUsername(String username) {
        LambdaQueryWrapper<User> userLambdaQueryWrapper = new LambdaQueryWrapper<>();
        userLambdaQueryWrapper.eq(User::getUsername,username);

        try {
            User user = userDao.getOne(userLambdaQueryWrapper);
            UserPasskey byUserId = userPasskeyDao.findByUserId(user.getId());
            return Optional.of(new ByteArray(ByteUtil.longToBytes(byUserId.getUserId())));
        }catch (Exception exception){
            log.info("Error Nullable:",exception.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public Optional<String> getUsernameForUserHandle(ByteArray userHandle) {
        LambdaQueryWrapper<UserPasskey> passkeyLambdaQueryWrapper = new LambdaQueryWrapper<>();
        passkeyLambdaQueryWrapper.eq(UserPasskey::getUserId,Long.valueOf(ByteUtil.bytesToLong(userHandle.getBytes())));

        List<UserPasskey> list = userPasskeyDao.list(passkeyLambdaQueryWrapper);
        if (list==null|| list.isEmpty()){
            return Optional.empty();
        }
        UserPasskey userPasskey = list.get(0);
        Long userId = userPasskey.getUserId();
        User byId = userDao.getById(userId);
        return Optional.ofNullable(byId.getUsername());
    }

    @Override
    public Optional<RegisteredCredential> lookup(ByteArray credentialId, ByteArray userHandle) {

        Long userId = ByteUtil.bytesToLong(userHandle.getBytes());
        LambdaQueryWrapper<UserPasskey> userPasskeyLambdaQueryWrapper = new LambdaQueryWrapper<>();
        userPasskeyLambdaQueryWrapper.eq(UserPasskey::getUserId,userId);
        userPasskeyLambdaQueryWrapper.eq(UserPasskey::getCredentialId,credentialId.getBase64());

        UserPasskey one = userPasskeyDao.getOne(userPasskeyLambdaQueryWrapper);
        Optional<UserPasskey> one1 = Optional.ofNullable(one);
        return one1.map(
                credential ->
                        RegisteredCredential.builder()
                                .credentialId(ByteArray.fromBase64(credential.getCredentialId()))
                                .userHandle(new ByteArray(ByteUtil.longToBytes(credential.getUserId())))
                                .publicKeyCose(ByteArray.fromBase64(credential.getPublicKey()))
                                .signatureCount(credential.getCount())
                                .build()
        );
    }

    @Override
    public Set<RegisteredCredential> lookupAll(ByteArray credentialId) {
        List<UserPasskey> auth = userPasskeyDao.findAllByCredentialId(credentialId.getBase64());
        return auth.stream()
                .map(
                        credential ->
                                RegisteredCredential.builder()
                                        .credentialId(ByteArray.fromBase64(credential.getCredentialId()))
                                        .userHandle(new ByteArray(ByteUtil.longToBytes(credential.getUserId())))
                                        .publicKeyCose(ByteArray.fromBase64(credential.getPublicKey()))
                                        .signatureCount(credential.getCount())
                                        .build())
                .collect(Collectors.toSet());
    }
}
