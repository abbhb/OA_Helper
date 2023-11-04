package com.qc.printers.common.user.service.aspect;

import com.qc.printers.common.common.CustomException;
import com.qc.printers.common.common.utils.AssertUtil;
import com.qc.printers.common.common.utils.ThreadLocalUtil;
import com.qc.printers.common.user.domain.dto.UserInfo;
import com.qc.printers.common.user.domain.entity.SysRole;
import com.qc.printers.common.user.service.UserInfoService;
import com.qc.printers.common.user.service.annotation.UserPermissionGradeCheck;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.core.annotation.Order;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Comparator;
import java.util.stream.Collectors;

/**
 * 优先级没测试，理论来说给1的话也比拦截器后生效
 */
//todo:阅读aop源码，看看为什么是这样的
@Aspect
@Component
@Order(4)
@Slf4j
public class UserPermissionGradeCheckAspect {

    @Autowired
    private UserInfoService userInfoService;

    @Pointcut("@annotation(userPermissionGradeCheck)")
    public void initLogAnno(UserPermissionGradeCheck userPermissionGradeCheck) {
    }

    @Before(value = "initLogAnno(userPermissionGradeCheck)", argNames = "point,userPermissionGradeCheck")
    public void doBefore(JoinPoint point, UserPermissionGradeCheck userPermissionGradeCheck) {
        //todo:判断原方法有没有needToken注解
        //通过SpEL获取接口参数对象属性值
        StandardEvaluationContext standardEvaluationContext = new StandardEvaluationContext(point.getArgs());
        standardEvaluationContext = setContextVariables(standardEvaluationContext, point);
        Long checkUserId = Long.valueOf(getElValue(userPermissionGradeCheck.checkUserId(), standardEvaluationContext));
        String lowMessage = userPermissionGradeCheck.logMessage();
        //权限排序判断业务代码
        UserInfo currentUser = ThreadLocalUtil.getCurrentUser();
        if (currentUser == null) {
            throw new CustomException("权限判断业务异常");
        }
        if (currentUser.getId().equals(checkUserId)) {
            // 自己对自己
            return;
        }
        AssertUtil.notEqual(currentUser.getSysRoles().size(), 0, lowMessage);
        SysRole highRole = currentUser.getSysRoles().stream().sorted(Comparator.comparingInt(SysRole::getRoleSort)).collect(Collectors.toList()).get(0);
        log.info("myrole{}", highRole);
        //判断该角色和下方的需要操作的用户对比
        SysRole needRole = userInfoService.getUserAllRole(checkUserId).stream().sorted(Comparator.comparingInt(SysRole::getRoleSort)).collect(Collectors.toList()).get(0);
        log.info("needRole{}", needRole);

        if (highRole.getRoleSort() >= needRole.getRoleSort()) {
            throw new CustomException(lowMessage);
        }
        //正常进行

    }

    private StandardEvaluationContext setContextVariables(StandardEvaluationContext standardEvaluationContext,
                                                          JoinPoint joinPoint) {

        Object[] args = joinPoint.getArgs();
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        Method targetMethod = methodSignature.getMethod();
        LocalVariableTableParameterNameDiscoverer parameterNameDiscoverer
                = new LocalVariableTableParameterNameDiscoverer();
        String[] parametersName = parameterNameDiscoverer.getParameterNames(targetMethod);

        if (args == null || args.length <= 0) {
            return standardEvaluationContext;
        }
        for (int i = 0; i < args.length; i++) {
            standardEvaluationContext.setVariable(parametersName[i], args[i]);
        }
        return standardEvaluationContext;
    }

    /**
     * 通过key SEL表达式获取值
     */
    private String getElValue(String key, StandardEvaluationContext context) {
        if (StringUtils.isBlank(key)) {
            return "";
        }
        ExpressionParser parser = new SpelExpressionParser();
        Expression exp = parser.parseExpression(key);
        String value = exp.getValue(context, String.class);

        return value;

    }


}
