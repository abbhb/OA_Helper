package com.qc.printers.custom.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.qc.printers.common.common.CustomException;
import com.qc.printers.common.user.dao.UserDao;
import com.qc.printers.common.user.domain.entity.SysDept;
import com.qc.printers.common.user.domain.entity.SysRole;
import com.qc.printers.common.user.domain.entity.SysRoleDept;
import com.qc.printers.common.user.domain.entity.User;
import com.qc.printers.common.user.service.ISysDeptService;
import com.qc.printers.common.user.service.ISysRoleDeptService;
import com.qc.printers.common.user.service.ISysRoleService;
import com.qc.printers.custom.user.domain.vo.response.dept.DeptManger;
import com.qc.printers.custom.user.service.DeptService;
import com.qc.printers.custom.user.utils.DeptMangerHierarchyBuilder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
public class DeptServiceImpl implements DeptService {

    @Autowired
    private ISysDeptService iSysDeptService;

    @Autowired
    private UserDao userDao;

    @Autowired
    private ISysRoleDeptService iSysRoleDeptService;

    @Autowired
    private ISysRoleService iSysRoleService;

    @Override
    public List<DeptManger> getDeptList() {
        List<SysDept> list = iSysDeptService.list();
        Set<SysRoleDept> sysRoleDepts = new HashSet<>(iSysRoleDeptService.list());
        Set<SysRole> sysRoles = new HashSet<>(iSysRoleService.list());
        list.sort((m1, m2) -> {
            Long order1 = m1.getId();
            Long order2 = m2.getId();
            return order1.compareTo(order2);
        });
        if (list.size() < 1) {
            throw new CustomException("系统异常，请添加根节点(id:0)");
        }
        DeptMangerHierarchyBuilder deptMangerHierarchyBuilder = new DeptMangerHierarchyBuilder(list, sysRoles, sysRoleDepts);
        List<DeptManger> deptMangers = deptMangerHierarchyBuilder.buildHierarchy();
        sortRecursion(deptMangers);

        return deptMangers;
    }

    private void sortRecursion(List<DeptManger> deptMangers) {
        deptMangers.sort((m1, m2) -> {
            Integer order1 = m1.getOrderNum();
            Integer order2 = m2.getOrderNum();
            return order1.compareTo(order2);
        });
        for (DeptManger deptManger :
                deptMangers) {
            if (deptManger.getChildren() != null) {
                if (deptManger.getChildren().size() > 0) {
                    sortRecursion(deptManger.getChildren());
                }
            }
        }
    }


    @Transactional
    @Override
    public String addDept(DeptManger deptManger) {
        //todo: 角色部门关系
        if (StringUtils.isEmpty(deptManger.getDeptName())) {
            throw new CustomException("请输入DeptName");
        }
        if (deptManger.getParentId() == null) {
            throw new CustomException("父级ID不能为空");
        }
        if (StringUtils.isEmpty(deptManger.getStatus())) {
            deptManger.setStatus("1");
        }
        SysDept sysDept = new DeptManger();
        BeanUtils.copyProperties(deptManger, sysDept);
        sysDept.setId(null);
        sysDept.setAncestors(getAncestrs(deptManger.getParentId()));
        iSysDeptService.save(sysDept);
        // 完善部门角色关系表
        if (deptManger.getRoles() != null && deptManger.getRoles().size() > 0) {
            for (SysRole sysRole :
                    deptManger.getRoles()) {
                SysRoleDept sysRoleDept = new SysRoleDept();
                sysRoleDept.setDeptId(sysDept.getId());
                sysRoleDept.setRoleId(sysRole.getId());
                iSysRoleDeptService.save(sysRoleDept);
            }
        }
        return "添加成功";
    }

    @Transactional
    public String getAncestrs(Long thisParentId) {
        if (thisParentId.equals(0L)) {
            return null;
        }
        String s = String.valueOf(thisParentId);
        SysDept sysDept = iSysDeptService.getById(thisParentId);
        ;
        while (sysDept != null && sysDept.getParentId() != null && sysDept.getParentId().equals(1L)) {
            s = String.valueOf(sysDept.getId()) + "," + s;
            sysDept = iSysDeptService.getById(sysDept.getParentId());
        }
        s = "1" + "," + s;
        //最终最前面加上1
        return s;
    }

    @Transactional
    @Override
    public String updateDept(DeptManger deptManger) {
        if (deptManger.getId() == null) {
            throw new CustomException("无操作对象");
        }
        if (StringUtils.isEmpty(deptManger.getDeptName())) {
            throw new CustomException("请输入DeptName");
        }
        if (deptManger.getParentId() == null) {
            throw new CustomException("父级ID不能为空");
        }
        if (StringUtils.isEmpty(deptManger.getStatus())) {
            deptManger.setStatus("1");
        }
        if (deptManger.getId().equals(1L)) {
            // 系统部门，禁止停用
            if (!deptManger.getStatus().equals("1")) {
                throw new CustomException("系统部门禁止停用");
            }
            deptManger.setParentId(0L);
        }
        LambdaUpdateWrapper<SysDept> lambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        lambdaUpdateWrapper.eq(SysDept::getId, deptManger.getId());
        lambdaUpdateWrapper.set(SysDept::getDeptName, deptManger.getDeptName());
        lambdaUpdateWrapper.set(SysDept::getEmail, deptManger.getEmail());
        // 前面的级别逗号分隔，前端传入
        lambdaUpdateWrapper.set(SysDept::getAncestors, deptManger.getAncestors());
        lambdaUpdateWrapper.set(SysDept::getParentId, deptManger.getParentId());
        lambdaUpdateWrapper.set(SysDept::getLeader, deptManger.getLeader());
        lambdaUpdateWrapper.set(SysDept::getOrderNum, deptManger.getOrderNum());
        lambdaUpdateWrapper.set(SysDept::getStatus, deptManger.getStatus());
        lambdaUpdateWrapper.set(SysDept::getPhone, deptManger.getPhone());
        lambdaUpdateWrapper.set(SysDept::getAncestors, getAncestrs(deptManger.getParentId()));
        iSysDeptService.update(lambdaUpdateWrapper);
        //还得更新角色关联表 way:removeReBuild
        LambdaQueryWrapper<SysRoleDept> sysRoleDeptLambdaQueryWrapper = new LambdaQueryWrapper<>();
        sysRoleDeptLambdaQueryWrapper.eq(SysRoleDept::getDeptId, deptManger.getId());
        iSysRoleDeptService.remove(sysRoleDeptLambdaQueryWrapper);
        //ReBuildNewRoleDept
        if (deptManger.getRoles() != null && deptManger.getRoles().size() > 0) {
            log.info("{}", deptManger.getRoles());
            for (SysRole sysRole :
                    deptManger.getRoles()) {
                SysRoleDept sysRoleDept = new SysRoleDept();
                sysRoleDept.setDeptId(deptManger.getId());
                sysRoleDept.setRoleId(sysRole.getId());
                iSysRoleDeptService.save(sysRoleDept);
            }
        }
        return "更新成功";
    }

    @Transactional
    @Override
    public String deleteDept(String id) {
        if (id.equals("1")) {
            throw new CustomException("系统部门禁止删除");
        }
        //需要确保该部门以及下属部门没有绑定用户
        LambdaQueryWrapper<SysDept> sysDeptLambdaQueryWrapper = new LambdaQueryWrapper<>();
        sysDeptLambdaQueryWrapper.eq(SysDept::getId, Long.valueOf(id)).or().like(SysDept::getAncestors, Long.valueOf(id));
        // list 就是要确认的，确认到哪个部门有用户
        // 都没有用户就全删除
        List<SysDept> list = iSysDeptService.list(sysDeptLambdaQueryWrapper);
        for (SysDept sysDept : list) {
            LambdaQueryWrapper<User> userLambdaQueryWrapper = new LambdaQueryWrapper<>();
            userLambdaQueryWrapper.eq(User::getDeptId, sysDept.getId());
            int count = userDao.count(userLambdaQueryWrapper);
            if (count != 0) {
                throw new CustomException("[" + sysDept.getDeptName() + "]部门下存在用户！禁止直接删除");
            }
        }
        //符合条件进行级联 删除(和菜单逻辑类似) 一并删除部门角色关联
        for (SysDept sysDept : list) {
            LambdaQueryWrapper<SysRoleDept> sysRoleDeptLambdaQueryWrapper = new LambdaQueryWrapper<>();
            sysRoleDeptLambdaQueryWrapper.eq(SysRoleDept::getDeptId, sysDept.getId());
            iSysRoleDeptService.remove(sysRoleDeptLambdaQueryWrapper);
            iSysDeptService.removeById(sysDept.getId());
        }
        return "删除成功";
    }

    @Override
    public List<DeptManger> getDeptListOnlyTree() {
        LambdaQueryWrapper<SysDept> sysDeptLambdaQueryWrapper = new LambdaQueryWrapper<>();
        sysDeptLambdaQueryWrapper.select(SysDept::getDeptName, SysDept::getId, SysDept::getParentId, SysDept::getStatus, SysDept::getOrderNum);
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

        return deptMangers;
    }

    @Override
    public List<DeptManger> listForBPM(String name) {
        LambdaQueryWrapper<SysDept> sysDeptLambdaQueryWrapper = new LambdaQueryWrapper<>();
        sysDeptLambdaQueryWrapper.like(StringUtils.isNotEmpty(name), SysDept::getDeptName, name);
        List<SysDept> list = iSysDeptService.list(sysDeptLambdaQueryWrapper);
        Set<SysRoleDept> sysRoleDepts = new HashSet<>(iSysRoleDeptService.list());
        Set<SysRole> sysRoles = new HashSet<>(iSysRoleService.list());
        list.sort((m1, m2) -> {
            Long order1 = m1.getId();
            Long order2 = m2.getId();
            return order1.compareTo(order2);
        });
        if (list.size() < 1) {
            throw new CustomException("系统异常，请添加根节点(id:0)");
        }
        DeptMangerHierarchyBuilder deptMangerHierarchyBuilder = new DeptMangerHierarchyBuilder(list, sysRoles, sysRoleDepts);
        List<DeptManger> deptMangers = deptMangerHierarchyBuilder.buildHierarchy();
        sortRecursion(deptMangers);
        return deptMangers;
    }


}
