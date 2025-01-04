package com.qc.printers.common.print.annotation;


import io.swagger.annotations.ApiOperation;

import java.lang.annotation.*;


@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface PrintDeviceRoleCheck {
    int[] role() default {1,2,3};//默认所有用户都有权限
    // 一定要作用在string上
    String deviceEl() default ""; // 设备id的el表达表达式

}
