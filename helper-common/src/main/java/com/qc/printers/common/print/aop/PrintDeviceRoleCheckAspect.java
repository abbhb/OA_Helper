package com.qc.printers.common.print.aop;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qc.printers.common.common.CustomException;
import com.qc.printers.common.common.annotation.DataScope;
import com.qc.printers.common.common.annotation.NeedToken;
import com.qc.printers.common.common.utils.SpElUtils;
import com.qc.printers.common.common.utils.StringUtils;
import com.qc.printers.common.common.utils.ThreadLocalUtil;
import com.qc.printers.common.print.annotation.PrintDeviceRoleCheck;
import com.qc.printers.common.print.dao.SysPrintDeviceUserDao;
import com.qc.printers.common.print.domain.entity.SysPrintDeviceUser;
import com.qc.printers.common.user.dao.SysRoleDataScopeDeptDao;
import com.qc.printers.common.user.domain.dto.DeptManger;
import com.qc.printers.common.user.domain.dto.UserInfo;
import com.qc.printers.common.user.domain.entity.SysDept;
import com.qc.printers.common.user.domain.entity.SysRole;
import com.qc.printers.common.user.domain.entity.User;
import com.qc.printers.common.user.domain.enums.DataScopeEnum;
import com.qc.printers.common.user.service.ISysDeptService;
import com.qc.printers.common.user.utils.DeptMangerHierarchyBuilder;
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
import java.util.OptionalInt;
import java.util.stream.Collectors;

import static com.qc.printers.common.user.domain.dto.DeptManger.sortRecursion;

/**
 * 过滤打印机权限
 *
 * @author ruoyi
 */
@Aspect
@Component
public class PrintDeviceRoleCheckAspect
{

    @Autowired
    private SysPrintDeviceUserDao sysPrintDeviceUserDao;


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
            throw new CustomException("请最少运行一个角色:print-device");
        }
        // 获取当前的用户
        UserInfo currentUser = ThreadLocalUtil.getCurrentUser();
        if (currentUser == null) {
            throw new CustomException("请先登录:print-device");
        }
        LambdaQueryWrapper<SysPrintDeviceUser> sysPrintDeviceUserLambdaQueryWrapper = new LambdaQueryWrapper<>();
        sysPrintDeviceUserLambdaQueryWrapper.eq(SysPrintDeviceUser::getPrintDeviceId,Long.valueOf(key));
        sysPrintDeviceUserLambdaQueryWrapper.eq(SysPrintDeviceUser::getUserId,currentUser.getId());
        SysPrintDeviceUser one = sysPrintDeviceUserDao.getOne(sysPrintDeviceUserLambdaQueryWrapper);
        if (one==null){
            throw new CustomException("你还没有权限进行此操作");
        }
        OptionalInt first = Arrays.stream(role).filter(value -> value == one.getRole()).findFirst();
        if (first.isEmpty()){
            throw new CustomException("你还没有权限进行此操作");
        }
    }


}