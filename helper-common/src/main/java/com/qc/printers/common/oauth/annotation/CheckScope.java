package com.qc.printers.common.oauth.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 权限检查注解
 */
@Retention(RetentionPolicy.RUNTIME)//运行时生效
@Target(ElementType.METHOD)//作用在方法上
public @interface CheckScope {
    /**
     * access_token
     *
     * @return
     */
    String token();

    /**
     * 客户端id
     *
     * @return
     */
    String cliendId();

    String needScope();
}
