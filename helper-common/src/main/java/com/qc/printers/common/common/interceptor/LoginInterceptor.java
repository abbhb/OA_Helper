package com.qc.printers.common.common.interceptor;


import com.qc.printers.common.common.Code;
import com.qc.printers.common.common.CustomException;
import com.qc.printers.common.common.annotation.NeedToken;
import com.qc.printers.common.common.utils.JWTUtil;
import com.qc.printers.common.common.utils.RedisUtils;
import com.qc.printers.common.common.utils.ThreadLocalUtil;
import com.qc.printers.common.user.domain.entity.User;
import com.qc.printers.common.user.service.IUserService;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;

@Slf4j
@Component
@Api("此拦截器用于获取用户基本信息存在threadlocal内,并且校验是否登录")
public class LoginInterceptor implements HandlerInterceptor {
    @Autowired
    private IUserService iUserService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (HttpMethod.OPTIONS.toString().equals(request.getMethod())) {
            System.out.println("OPTIONS请求，放行");
            System.out.println(request.getRequestURI());
            return true;
        }
        //如果不是映射到方法直接通过
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }
        HandlerMethod handlerMethod = (HandlerMethod) handler;
        Method method = handlerMethod.getMethod();
        if (method.getAnnotation(NeedToken.class) == null){
            //如果没有前置条件 需要登陆
            //权限校验直接通过
            return true;
        }
        final String authHeader = request.getHeader(JWTUtil.AUTH_HEADER_KEY);
        log.info("## authHeader= {}", authHeader);
        if (StringUtils.isBlank(authHeader) || !authHeader.startsWith(JWTUtil.TOKEN_PREFIX)) {
            log.info("### 用户未登录，请先登录 ###");
            throw new CustomException("请先登录!", Code.DEL_TOKEN);
        }
        // 获取token
        final String token = authHeader.substring(7);
        if (StringUtils.isEmpty(token)){
            //没有JWTtoken
            throw new CustomException("请先登录!",Code.DEL_TOKEN);
        }
        log.info("### 解析token= {}", token);
        String userId = RedisUtils.get(token);
        if (StringUtils.isEmpty(userId)){
            throw new CustomException("认证失败",Code.DEL_TOKEN);
        }
        User userByToken = iUserService.getById(Long.valueOf(userId));
        ThreadLocalUtil.addCurrentUser(userByToken);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        //防止内存泄露，对ThreadLocal里的对象进行清除
        ThreadLocalUtil.remove();
    }
}
