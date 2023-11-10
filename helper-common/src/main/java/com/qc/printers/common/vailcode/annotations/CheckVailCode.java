package com.qc.printers.common.vailcode.annotations;


import com.qc.printers.common.vailcode.domain.enums.VailType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)//运行时生效
@Target(ElementType.METHOD)//作用在方法上
public @interface CheckVailCode {

    VailType type() default VailType.VAILCODE;


    String value() default "";

    /**
     * 验证码的key
     *
     * @return
     */
    String key();
}
