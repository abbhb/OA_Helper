package com.qc.printers.common.oauth.service.aspect;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qc.printers.common.common.MyString;
import com.qc.printers.common.common.utils.RedisUtils;
import com.qc.printers.common.common.utils.SpElUtils;
import com.qc.printers.common.oauth.annotation.CheckScope;
import com.qc.printers.common.oauth.dao.SysOauthDao;
import com.qc.printers.common.oauth.dao.SysOauthUserDao;
import com.qc.printers.common.oauth.domain.dto.AccessToken;
import com.qc.printers.common.oauth.domain.entity.SysOauth;
import com.qc.printers.common.oauth.domain.entity.SysOauthUser;
import com.qc.printers.common.oauth.exception.OauthException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * oauth2.0 权限校验
 */
@Slf4j
@Aspect
@Component
@Order(0)
public class CheckScopeAspect {
    @Autowired
    private SysOauthUserDao sysOauthUserDao;

    @Autowired
    private SysOauthDao sysOauthDao;

    @Around("@annotation(com.qc.printers.common.oauth.annotation.CheckScope)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {

        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        CheckScope checkVailCode = method.getAnnotation(CheckScope.class);
        Object[] args = joinPoint.getArgs();
        // 获取type参数
        String cliendId = SpElUtils.parseSpEl(method, args, checkVailCode.cliendId());
        String token = SpElUtils.parseSpEl(method, args, checkVailCode.token());
        String needScope = checkVailCode.needScope();
        if (StringUtils.isEmpty(cliendId) || StringUtils.isEmpty(token)) {
            throw new OauthException("cliendId or token is null");
        }
        AccessToken accessToken = RedisUtils.get(MyString.oauth_access_token + token, AccessToken.class);
        if (accessToken == null) {
            throw new OauthException("token is invalid");
        }
        if (!accessToken.getClientId().equals(cliendId)) {
            throw new OauthException("cliendId is invalid");
        }
        LambdaQueryWrapper<SysOauth> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(SysOauth::getClientId, cliendId);
        SysOauth one = sysOauthDao.getOne(lambdaQueryWrapper);
        if (one == null) {
            throw new OauthException("cliendId is invalid");
        }

        LambdaQueryWrapper<SysOauthUser> sysOauthUserLambdaQueryWrapper = new LambdaQueryWrapper<>();
        sysOauthUserLambdaQueryWrapper.eq(SysOauthUser::getOauthId, one.getId());
        sysOauthUserLambdaQueryWrapper.eq(SysOauthUser::getUserId, accessToken.getUserId());
        SysOauthUser one1 = sysOauthUserDao.getOne(sysOauthUserLambdaQueryWrapper);
        if (one1 == null) {
            throw new OauthException("token is invalid");
        }
        String scope = one1.getScope();
        if (StringUtils.isEmpty(scope)) {
            throw new OauthException("当前未授权");
        }
        boolean b = Arrays.asList(scope.split(",")).contains(needScope);
        if (!b) {
            throw new OauthException("当前未授权,无法获取信息");
        }
        //无异常继续执行
        return joinPoint.proceed();
    }
}
