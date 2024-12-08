package com.qc.printers.custom.webauthn.service;

import cn.hutool.core.util.ByteUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.qc.printers.common.common.CustomException;
import com.qc.printers.common.common.utils.RedisUtils;
import com.qc.printers.common.common.utils.StringUtils;
import com.qc.printers.common.common.utils.ThreadLocalUtil;
import com.qc.printers.common.user.dao.UserDao;
import com.qc.printers.common.user.domain.dto.UserInfo;
import com.qc.printers.common.webauthn.dao.UserPasskeyDao;
import com.qc.printers.common.webauthn.entity.UserPasskey;
import com.qc.printers.common.webauthn.vo.req.ActionPasskeyRegistrationReq;
import com.yubico.webauthn.*;
import com.yubico.webauthn.data.*;
import com.yubico.webauthn.exception.AssertionFailedException;
import com.yubico.webauthn.exception.RegistrationFailedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RequiredArgsConstructor
@Slf4j
@Service
public class PasskeyAuthorizationService {



    private final UserDao userDao;

    private final UserPasskeyDao userPasskeyDao;

    private final RelyingParty relyingParty;
    private final String REDIS_PASSKEY_REGISTRATION_KEY = "passkey:registration";
    private final String REDIS_PASSKEY_ASSERTION_KEY = "passkey:assertion";

    public String startPasskeyRegistration() {

        UserInfo currentUser = ThreadLocalUtil.getCurrentUser();
        var user = userDao.getById(currentUser.getId());

        var options = relyingParty.startRegistration(StartRegistrationOptions.builder()
                .user(UserIdentity.builder()
                        .name(user.getUsername())
                        .displayName(user.getUsername())
                        .id(new ByteArray(ByteUtil.longToBytes(user.getId())))
                        .build())
                .authenticatorSelection(AuthenticatorSelectionCriteria.builder()
                        .residentKey(ResidentKeyRequirement.REQUIRED)
                        .build())
                .build());

        try {
            RedisUtils.hset(REDIS_PASSKEY_REGISTRATION_KEY, String.valueOf(user.getId()), options.toJson());
        } catch (JsonProcessingException e) {
            log.error("startPasskeyRegistration:",e);
            throw new RuntimeException(e);
        }

        try {
            return options.toCredentialsCreateJson();
        } catch (JsonProcessingException e) {
            log.error("startPasskeyRegistration2:",e);

            throw new RuntimeException(e);
        }
    }

    public List<UserPasskey> listPasskeyRegistrations(){
        UserInfo currentUser = ThreadLocalUtil.getCurrentUser();
        LambdaQueryWrapper<UserPasskey> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(UserPasskey::getUserId,currentUser.getId());
        return userPasskeyDao.list(lambdaQueryWrapper);
    }

    @Transactional
    public UserPasskey finishPasskeyRegistration(String credential) {
        UserInfo currentUser = ThreadLocalUtil.getCurrentUser();
        var user = userDao.getById(currentUser.getId());

        PublicKeyCredential<AuthenticatorAttestationResponse, ClientRegistrationExtensionOutputs> pkc = null;
        try {
            String decodedUrl = URLDecoder.decode(credential, StandardCharsets.UTF_8);
            pkc = PublicKeyCredential.parseRegistrationResponseJson(decodedUrl);
        } catch (IOException e) {
            log.error("finishPasskeyRegistration-1:",e);
            throw new RuntimeException(e);
        }

        PublicKeyCredentialCreationOptions request = null;
        try {
            request = PublicKeyCredentialCreationOptions.fromJson((String) RedisUtils.hget(REDIS_PASSKEY_REGISTRATION_KEY, String.valueOf(user.getId()),String.class));
        } catch (JsonProcessingException e) {
            log.error("finishPasskeyRegistration-2:",e);
            throw new RuntimeException(e);
        }

        RegistrationResult result = null;
        try {
            result = relyingParty.finishRegistration(FinishRegistrationOptions.builder()
                    .request(request)
                    .response(pkc)
                    .build());
        } catch (RegistrationFailedException e) {
            log.error("finishPasskeyRegistration-3:",e);
            throw new RuntimeException(e);
        }

        RedisUtils.hdel(REDIS_PASSKEY_REGISTRATION_KEY, String.valueOf(user.getId()));

        UserPasskey userPasskey = storeCredential(user.getId(), request, result);
        if (userPasskey==null){
            throw new CustomException("注册Passkey验证器失败");
        }
        userPasskey.setPublicKey(null);
        userPasskey.setId(null);
        return userPasskey;
    }

    public String startPasskeyAssertion(String identifier) {
        var options = relyingParty.startAssertion(StartAssertionOptions.builder().build());

        try {
            RedisUtils.hset(REDIS_PASSKEY_ASSERTION_KEY, identifier, options.toJson());
        } catch (JsonProcessingException e) {
            log.error("startPasskeyAssertion-1:",e);
            throw new RuntimeException(e);
        }

        try {
            return options.toCredentialsGetJson();
        } catch (JsonProcessingException e) {
            log.error("startPasskeyAssertion-2:",e);

            throw new RuntimeException(e);
        }
    }

    @Transactional
    public long finishPasskeyAssertion(String identifier, String credential) {
        AssertionRequest request = null;
        try {
            request = AssertionRequest.fromJson((String) RedisUtils.hget(REDIS_PASSKEY_ASSERTION_KEY, identifier,String.class));
        } catch (JsonProcessingException e) {
            log.error("finishPasskeyAssertion fromJson:",e);
            throw new RuntimeException(e);
        }
        PublicKeyCredential<AuthenticatorAssertionResponse, ClientAssertionExtensionOutputs> pkc = null;
        try {
            String decodedUrl = URLDecoder.decode(credential, StandardCharsets.UTF_8);

            pkc = PublicKeyCredential.parseAssertionResponseJson(decodedUrl);
        } catch (IOException e) {
            log.error("finishPasskeyAssertion parseAssertionResponseJson:",e);

            throw new RuntimeException(e);
        }

        AssertionResult result = null;
        try {
            result = relyingParty.finishAssertion(FinishAssertionOptions.builder()
                    .request(request)
                    .response(pkc)
                    .build());
        } catch (AssertionFailedException e) {
            log.error("finishPasskeyAssertion finishAssertion:",e);
            throw new RuntimeException(e);
        }

        RedisUtils.hdel(REDIS_PASSKEY_ASSERTION_KEY, identifier);

        if (!result.isSuccess()) {
            log.warn("finishPasskeyAssertion Verify failed!");
            throw new CustomException("Verify failed");
        }
        long userID = ByteUtil.bytesToLong(result.getUserHandle().getBytes());

        var user = userDao.getById(userID);

        updateCredential(user.getId(), result.getCredentialId(), result);

        return user.getId();
    }

    @Transactional
    public UserPasskey storeCredential(long id,
                                @NotNull PublicKeyCredentialCreationOptions request,
                                @NotNull RegistrationResult result) {
        UserPasskey userPasskey = fromFinishPasskeyRegistration(id, request, result);
        userPasskeyDao.save(userPasskey);
        return userPasskey;

    }

    @Transactional
    public void updateCredential(long id,
                                 @NotNull ByteArray credentialId,
                                 @NotNull AssertionResult result) {
        LambdaQueryWrapper<UserPasskey> userPasskeyLambdaQueryWrapper = new LambdaQueryWrapper<>();
        userPasskeyLambdaQueryWrapper.eq(UserPasskey::getUserId,id);
        userPasskeyLambdaQueryWrapper.eq(UserPasskey::getCredentialId,credentialId.getBase64());
        var entity = userPasskeyDao.getOne(userPasskeyLambdaQueryWrapper);
        entity.setCount(result.getSignatureCount());
        userPasskeyDao.updateById(entity);
    }

    @NotNull
    private static UserPasskey fromFinishPasskeyRegistration(long id,
                                                             PublicKeyCredentialCreationOptions request,
                                                             RegistrationResult result) {
//        CredentialRegistration.builder()
//                .userIdentity(request.getUser())
//                .transports(result.getKeyId().getTransports().orElseGet(TreeSet::new))
//                .registration(Clock.systemUTC().instant())
//                .credential(RegisteredCredential.builder()
//                        .credentialId(result.getKeyId().getId())
//                        .userHandle(request.getUser().getId())
//                        .publicKeyCose(result.getPublicKeyCose())
//                        .signatureCount(result.getSignatureCount())
//                        .build())
//                .build()

        return UserPasskey.builder()
                .userId(id)
                .count(result.getSignatureCount())
                .publicKey(result.getPublicKeyCose().getBase64())
                .credentialId(result.getKeyId().getId().getBase64())
                .name(String.format("新认证器-%s", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))))
                .build();
    }
    @Transactional
    public String deletePasskeyRegistration(String credentialId){
        if (StringUtils.isEmpty(credentialId)){
            throw new CustomException("不存在的认证器");
        }
        LambdaQueryWrapper<UserPasskey> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(UserPasskey::getCredentialId,credentialId);
        userPasskeyDao.remove(lambdaQueryWrapper);
        return "删除认证器成功";
    }

    @Transactional
    public String reNamePasskeyRegistration(String credentialId,String label){
        if (StringUtils.isEmpty(credentialId)){
            throw new CustomException("不存在的认证器");
        }
        if (StringUtils.isEmpty(label)){
            throw new CustomException("名称不能为空");
        }
        LambdaUpdateWrapper<UserPasskey> lambdaQueryWrapper = new LambdaUpdateWrapper<>();
        lambdaQueryWrapper.eq(UserPasskey::getCredentialId,credentialId);
        lambdaQueryWrapper.set(UserPasskey::getName,label);
        userPasskeyDao.update(lambdaQueryWrapper);
        return "重命名认证器成功";
    }

    @Transactional
    public String actionPasskeyRegistration(ActionPasskeyRegistrationReq data) {
        if (data==null){
            throw new CustomException("无效的操作");
        }
        if (StringUtils.isEmpty(data.getAction())){
            throw new CustomException("无效的操作");
        }
        return switch (data.getAction()) {
            case "delete" -> deletePasskeyRegistration(data.getCredentialId());
            case "rename" -> reNamePasskeyRegistration(data.getCredentialId(), data.getLabel());
            default -> "未定义的操作";
        };
    }
}