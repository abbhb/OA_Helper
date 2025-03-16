package com.qc.printers.common.common.aspect;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qc.printers.common.common.CustomException;
import com.qc.printers.common.common.annotation.DataScope;
import com.qc.printers.common.common.service.DataScopeService;
import com.qc.printers.common.common.utils.StringUtils;
import com.qc.printers.common.common.utils.ThreadLocalUtil;
import com.qc.printers.common.user.dao.SysRoleDataScopeDeptDao;
import com.qc.printers.common.user.domain.dto.DeptManger;
import com.qc.printers.common.user.domain.dto.UserInfo;
import com.qc.printers.common.user.domain.entity.SysDept;
import com.qc.printers.common.user.domain.entity.SysRole;
import com.qc.printers.common.user.domain.entity.SysRoleDataScopeDept;
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
import java.util.List;
import java.util.stream.Collectors;

import static com.qc.printers.common.user.domain.dto.DeptManger.sortRecursion;

/**
 * 数据过滤处理
 *
 * @author ruoyi
 */
@Aspect
@Component
public class DataScopeAspect
{

    /**
     * 数据权限过滤关键字
     */
    public static final String DATA_SCOPE = "dataScope";


    @Autowired
    private DataScopeService dataScopeService;


    @Pointcut("@annotation(com.qc.printers.common.common.annotation.DataScope)")
    public void dataScopePointCut()
    {
    }

    @Before("dataScopePointCut()")
    public void doBefore(JoinPoint point) throws Throwable
    {
        handleDataScope(point);
    }

    protected void handleDataScope(final JoinPoint joinPoint)
    {
        // 获得注解
        DataScope controllerDataScope = getAnnotationLog(joinPoint);
        if (controllerDataScope == null)
        {
            return;
        }
        // 获取当前的用户
        UserInfo currentUser = ThreadLocalUtil.getCurrentUser();
        if (currentUser != null)
        {
            // 如果是超级管理员，则不过滤数据
            if (!currentUser.getSysRoles().stream().anyMatch(sysRole -> sysRole.getRoleKey().equals("superadmin")))
            {
                dataScopeFilter(joinPoint, currentUser, controllerDataScope.deptAlias(),
                        controllerDataScope.userAlias());
            }
        }
    }



    /**
     * 数据范围过滤
     *
     * @param joinPoint 切点
     * @param user 用户
     * @param deptAlias 部门别名
     * @param userAlias 用户别名
     */
    public void dataScopeFilter(JoinPoint joinPoint, UserInfo user, String deptAlias, String userAlias)
    {
        String dataScopeSqlString = dataScopeService.getDataScopeSqlString(user, deptAlias, userAlias);
        if (StringUtils.isNotEmpty(dataScopeSqlString))
        {
            Object params = joinPoint.getArgs()[0];
            if (StringUtils.isNotNull(params))
            {
                if (params instanceof SysDept){
                    SysDept baseEntity = (SysDept) params;
                    baseEntity.setExistSql(" (" + dataScopeSqlString.substring(4) + ")");
                }
                if (params instanceof User){
                    User baseEntity = (User) params;
                    baseEntity.setExistSql(" (" + dataScopeSqlString.substring(4) + ")");
                }
            }
        }
    }





    /**
     * 是否存在注解，如果存在就获取
     */
    private DataScope getAnnotationLog(JoinPoint joinPoint)
    {
        Signature signature = joinPoint.getSignature();
        MethodSignature methodSignature = (MethodSignature) signature;
        Method method = methodSignature.getMethod();

        if (method != null)
        {
            return method.getAnnotation(DataScope.class);
        }
        return null;
    }


}