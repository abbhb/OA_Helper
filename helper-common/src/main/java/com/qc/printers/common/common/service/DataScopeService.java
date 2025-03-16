package com.qc.printers.common.common.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qc.printers.common.common.CustomException;
import com.qc.printers.common.common.utils.StringUtils;
import com.qc.printers.common.user.dao.SysRoleDataScopeDeptDao;
import com.qc.printers.common.user.domain.dto.DeptManger;
import com.qc.printers.common.user.domain.dto.UserInfo;
import com.qc.printers.common.user.domain.entity.SysDept;
import com.qc.printers.common.user.domain.entity.SysRole;
import com.qc.printers.common.user.domain.enums.DataScopeEnum;
import com.qc.printers.common.user.service.ISysDeptService;
import com.qc.printers.common.user.utils.DeptMangerHierarchyBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import static com.qc.printers.common.user.domain.dto.DeptManger.sortRecursion;

@Slf4j
@Service
public class DataScopeService {
    @Autowired
    private ISysDeptService iSysDeptService;


    /**
     * apply 不能为空，否则会导致无数据的，一定要判空！
     * userLambdaQueryWrapper.apply(StringUtils.isNotEmpty(dataScopeSqlString),dataScopeSqlString);
     * @param user
     * @param deptAlias
     * @param userAlias
     * @return
     */
    public String getDataScopeSqlString(UserInfo user, String deptAlias, String userAlias){
        StringBuilder sqlString = new StringBuilder();

        List<Long> idsWithDescendants = null;
        boolean addSelf = false;
        for (SysRole role : user.getSysRoles())
        {
            Integer dataScope = role.getDataScope();
            if (DataScopeEnum.ALL_DATA_SCOPE.getType().equals(dataScope))
            {
                addSelf = false;
                sqlString = new StringBuilder();
                break;
            }
            else if (DataScopeEnum.CUSTOM_DATA_SCOPE.getType().equals(dataScope))
            {
                if (role.getId()!=null){
                    addSelf = true;
                    if (StringUtils.isNotEmpty(deptAlias)){
                        sqlString.append(StringUtils.format(
                                " OR {}.id IN ( SELECT dept_id FROM sys_role_data_scope_dept WHERE role_id = {} ) ", deptAlias,
                                role.getId()));
                    }else if (StringUtils.isNotEmpty(userAlias)){
                        sqlString.append(StringUtils.format(
                                " OR {}.dept_id IN ( SELECT dept_id FROM sys_role_data_scope_dept WHERE role_id = {} ) ", userAlias,
                                role.getId()));
                    }

                }
            }
            else if (DataScopeEnum.CURRENT_DEPT_DATA_SCOPE.getType().equals(dataScope))
            {
                // 此处需要单独处理，包含当前部门和下级部门的列表
                // 因为某个用户的此项是固定的，只查一次就行
                if (idsWithDescendants==null){
                    LambdaQueryWrapper<SysDept> sysDeptLambdaQueryWrapper = new LambdaQueryWrapper<>();
                    sysDeptLambdaQueryWrapper.select(SysDept::getDeptName, SysDept::getId, SysDept::getParentId, SysDept::getStatus, SysDept::getOrderNum,SysDept::getDeptNameAll);
                    List<SysDept> list = iSysDeptService.list(sysDeptLambdaQueryWrapper);
                    list.sort((m1, m2) -> {
                        Long order1 = m1.getId();
                        Long order2 = m2.getId();
                        return order1.compareTo(order2);
                    });
                    if (list.size() < 1) {
                        throw new CustomException("系统异常，请添加根节点(id:0)");
                    }
                    DeptMangerHierarchyBuilder deptMangerHierarchyBuilder = new DeptMangerHierarchyBuilder(list, null, null, 0);
                    List<DeptManger> deptMangers = deptMangerHierarchyBuilder.buildHierarchy();

                    sortRecursion(deptMangers);

                    DeptManger deptManagerById = DeptManger.findDeptManagerById(deptMangers, user.getDeptId());
                    if (deptManagerById==null){
                        throw new CustomException("业务异常");
                    }
                    idsWithDescendants = DeptManger.getIdsWithDescendants(deptManagerById);
                }
                if (idsWithDescendants==null||idsWithDescendants.size()==0){
                    // 最少一条是自己的部门
                    throw new CustomException("业务异常");
                }
                if (idsWithDescendants.size()==1){
                    if (StringUtils.isNotEmpty(deptAlias)){
                        sqlString.append(StringUtils.format(
                                " OR {}.id = {} ",
                                deptAlias, user.getDeptId()));
                    }else if (StringUtils.isNotEmpty(userAlias)){
                        sqlString.append(StringUtils.format(
                                " OR {}.dept_id = {} ",
                                userAlias, user.getDeptId()));
                    }

                }else {
                    String ids = idsWithDescendants.stream()
                            .map(String::valueOf) // 将Long转换为String
                            .collect(Collectors.joining(","));
                    if (StringUtils.isNotEmpty(deptAlias)){
                        sqlString.append(StringUtils.format(
                                " OR {}.id IN ( {} )",
                                deptAlias, ids));
                    }else if (StringUtils.isNotEmpty(userAlias)){
                        sqlString.append(StringUtils.format(
                                " OR {}.dept_id IN ( {} )",
                                userAlias, ids));
                    }

                }

            }
            else if (DataScopeEnum.ONLY_CURRENT_USER_DATA_SCOPE.getType().equals(dataScope))
            {
                if (StringUtils.isNotBlank(userAlias))
                {
                    sqlString.append(StringUtils.format(" OR {}.id = {} ", userAlias, user.getId()));
                }
                else
                {
                    // 数据权限为仅本人且没有userAlias别名不查询任何数据
                    sqlString.append(" OR 1=0 ");
                }
            }
        }
        if (addSelf){
            if (StringUtils.isNotBlank(userAlias))
            {
                sqlString.append(StringUtils.format(" OR {}.id = {} ", userAlias, user.getId()));
            }
            // 可以的话就补，补不了也就算了
        }

        if (StringUtils.isNotBlank(sqlString.toString()))
        {
            return " (" + sqlString.substring(4) + ")";
        }
        return "";
    }
}
