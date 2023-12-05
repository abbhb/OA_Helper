package com.qc.printers.common.vailcode.service.aspect;

import com.qc.printers.common.vailcode.annotations.CheckVailCode;
import com.qc.printers.common.vailcode.domain.enums.VailType;
import com.qc.printers.common.vailcode.service.strategy.VailCodeVerifyHandel;
import com.qc.printers.common.vailcode.service.strategy.VailCodeVerifyHandelFactory;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Slf4j
@Aspect
@Component
@Order(0)//校验前判断是否启用了验证码，没启用直接跳过，如果启用了就校验验证码成功的成功码，1一次有效，1分钟内
public class CheckAspect {
    @Autowired
    private VailCodeVerifyHandelFactory vailCodeVerifyHandelFactory;

    @Around("@annotation(com.qc.printers.common.vailcode.annotations.CheckVailCode)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {

        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        CheckVailCode checkVailCode = method.getAnnotation(CheckVailCode.class);
        VailType target = checkVailCode.type();
        // 获取type参数
        VailCodeVerifyHandel instance = vailCodeVerifyHandelFactory.getInstance(target.getType());
        instance.verify(method, joinPoint.getArgs(), checkVailCode);
        // 无异常就继续

        return joinPoint.proceed();
    }
}
