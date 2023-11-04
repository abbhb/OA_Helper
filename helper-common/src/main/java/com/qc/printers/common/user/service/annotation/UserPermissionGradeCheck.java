package com.qc.printers.common.user.service.annotation;

import java.lang.annotation.*;


/**
 * 用来判断用户最高的角色的排序是不是比要操作用户的最高角色高，没有就抛出异常，权限不够
 * 要求有该注解的必须又needtoken注解，不然threadlocal里为空，拿不到当前用户的数据
 */
@Target({ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface UserPermissionGradeCheck {

    String checkUserId();

    String logMessage() default "权限不足";
}
