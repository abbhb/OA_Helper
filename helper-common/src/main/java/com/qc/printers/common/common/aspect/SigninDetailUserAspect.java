package com.qc.printers.common.common.aspect;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qc.printers.common.common.CustomException;
import com.qc.printers.common.common.annotation.RedissonLock;
import com.qc.printers.common.common.annotation.SigninDetailUserCheck;
import com.qc.printers.common.common.service.DataScopeService;
import com.qc.printers.common.common.service.LockService;
import com.qc.printers.common.common.utils.SpElUtils;
import com.qc.printers.common.common.utils.StringUtils;
import com.qc.printers.common.common.utils.ThreadLocalUtil;
import com.qc.printers.common.user.dao.UserDao;
import com.qc.printers.common.user.domain.dto.UserInfo;
import com.qc.printers.common.user.domain.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * Description: 考勤详情校验切片
 * Author: <a href="https://github.com/abbhb">abbhb</a>
 * Date: 2025-03-16
 */
@Slf4j
@Aspect
@Component
public class SigninDetailUserAspect {
    @Autowired
    private UserDao userDao;


    @Autowired
    private DataScopeService dataScopeService;

    @Around("@annotation(com.qc.printers.common.common.annotation.SigninDetailUserCheck)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        SigninDetailUserCheck signinDetailUserCheck = method.getAnnotation(SigninDetailUserCheck.class);
        String userId = SpElUtils.parseSpEl(method, joinPoint.getArgs(), signinDetailUserCheck.userIdKey());
        log.info("SigninDetailUserCheck-userId:{}", userId);
        // todo: 需要验证Long类型是否能被正常解析
        if (StrUtil.isBlank(userId)) {
            throw new RuntimeException("userId is null");
        }
        UserInfo currentUser = ThreadLocalUtil.getCurrentUser();

        String dataScopeSqlString = dataScopeService.getDataScopeSqlString(currentUser, "", "user");
        LambdaQueryWrapper<User> userLambdaQueryWrapper = new LambdaQueryWrapper<>();
        userLambdaQueryWrapper.eq(User::getId,Long.valueOf(userId));
        // 拼接额外SQL
        log.info("SigninDetailUserAspect-dataScopeSqlString:{}", dataScopeSqlString);
        userLambdaQueryWrapper.apply(StringUtils.isNotEmpty(dataScopeSqlString),dataScopeSqlString);
        long count = userDao.count(userLambdaQueryWrapper);
        if (count < 1L){
            throw new CustomException("Your Permission too small.");
        }
        return joinPoint.proceed();
    }
}
