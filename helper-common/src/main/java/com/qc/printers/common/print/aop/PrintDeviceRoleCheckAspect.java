package com.qc.printers.common.print.aop;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qc.printers.common.common.CustomException;
import com.qc.printers.common.common.annotation.NeedToken;
import com.qc.printers.common.common.utils.SpElUtils;
import com.qc.printers.common.common.utils.ThreadLocalUtil;
import com.qc.printers.common.print.annotation.PrintDeviceRoleCheck;
import com.qc.printers.common.print.dao.SysPrintDeviceLinkDao;
import com.qc.printers.common.print.domain.entity.SysPrintDeviceLink;
import com.qc.printers.common.user.domain.dto.UserInfo;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

/**
 * 过滤打印机权限
 *
 * */
@Aspect
@Component
public class PrintDeviceRoleCheckAspect
{

    @Autowired
    private SysPrintDeviceLinkDao sysPrintDeviceLinkDao;


    @Pointcut("@annotation(com.qc.printers.common.print.annotation.PrintDeviceRoleCheck)")
    public void printDeviceRolePointCut()
    {
    }

    @Before("printDeviceRolePointCut()")
    public void doBefore(JoinPoint point) throws Throwable
    {
        handleDataScope(point);
    }

    protected void handleDataScope(final JoinPoint joinPoint)
    {

        Signature signature = joinPoint.getSignature();
        MethodSignature methodSignature = (MethodSignature) signature;
        Method method = methodSignature.getMethod();
        if (method==null){
            throw new CustomException("打印设备-异常apo");
        }
        PrintDeviceRoleCheck controllerDataScope = (PrintDeviceRoleCheck) method.getAnnotation(PrintDeviceRoleCheck.class);
        if (method.getAnnotation(NeedToken.class) == null) {
            throw new CustomException("请与needtoken一同使用");
        }
        if (controllerDataScope == null)
        {
            throw new CustomException("打印设备-异常apo");
        }
        String key = SpElUtils.parseSpEl(method, joinPoint.getArgs(), controllerDataScope.deviceEl());
        int[] role = controllerDataScope.role();
        if(role==null|| role.length==0){
            throw new CustomException("请最少允许一个角色:print-device");
        }
        // 获取当前的用户
        UserInfo currentUser = ThreadLocalUtil.getCurrentUser();
        if (currentUser == null) {
            throw new CustomException("请先登录:print-device");
        }
        LambdaQueryWrapper<SysPrintDeviceLink> sysPrintDeviceUserLambdaQueryWrapper = new LambdaQueryWrapper<>();
        sysPrintDeviceUserLambdaQueryWrapper.eq(SysPrintDeviceLink::getPrintDeviceId,Long.valueOf(key));
        sysPrintDeviceUserLambdaQueryWrapper.and(
                wrapper -> wrapper.eq(
                        SysPrintDeviceLink::getLinkId, currentUser.getId()
                        )
                .eq(SysPrintDeviceLink::getLinkType, 1)
                .or().eq(SysPrintDeviceLink::getLinkType, 2)
                .eq(SysPrintDeviceLink::getLinkId, currentUser.getDeptId()
                )
        );
        List<SysPrintDeviceLink> deviceUserList = sysPrintDeviceLinkDao.list(sysPrintDeviceUserLambdaQueryWrapper);
        if (deviceUserList==null||deviceUserList.isEmpty()){
            throw new CustomException("你还没有权限进行此操作");
        }
        boolean anyMatch = deviceUserList.stream()
                .map(SysPrintDeviceLink::getRole)
                .anyMatch(roleValue -> Arrays.stream(role).anyMatch(value -> value == roleValue));
        if (!anyMatch){
            throw new CustomException("你还没有权限进行此操作");
        }
    }


}