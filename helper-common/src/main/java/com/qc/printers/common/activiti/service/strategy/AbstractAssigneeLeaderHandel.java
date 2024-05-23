package com.qc.printers.common.activiti.service.strategy;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qc.printers.common.common.CustomException;
import com.qc.printers.common.user.dao.UserDao;
import com.qc.printers.common.user.domain.entity.SysDept;
import com.qc.printers.common.user.domain.entity.User;
import com.qc.printers.common.user.service.ISysDeptService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Map;

@Component
public abstract class AbstractAssigneeLeaderHandel {
    @Autowired
    private UserDao userDao;

    @Autowired
    private ISysDeptService iSysDeptService;

    @PostConstruct
    private void init() {
        //实现类都会继承该抽象模板，都会注册为组件，在注册时在工厂注册
        AssigneeLeaderHandelFactory.register(getAssigneeLeaderType(), this);

    }

    /**
     * 消息类型
     */
    abstract String getAssigneeLeaderType();

    public abstract void addVariables(Map<String, Object> variables, String userId);

    public SysDept getSysDeptByUser(String userId) {
        if (StringUtils.isEmpty(userId)) {
            throw new CustomException("用户id不存在");
        }
        User user = userDao.getById(Long.valueOf(userId));
        if (user == null) {
            throw new CustomException("用户不存在");
        }

        SysDept sysDept = iSysDeptService.getById(user.getDeptId());
        if (sysDept == null) {
            throw new CustomException("部门对象不存在");
        }
        return sysDept;
    }

    public User getNotNullLeader(SysDept sysDept, int deep) {
        String ancestors = sysDept.getAncestors();
        if (StringUtils.isEmpty(ancestors)) {
            if (!sysDept.getParentId().equals(0L)) {
                throw new CustomException("异常情况,联系管理员排除--error:789");
            }
            if (sysDept.getLeaderId() == null) {
                throw new CustomException("无可用负责人--error:790");
            }
            User byId = userDao.getById(sysDept.getLeaderId());
            if (byId == null) {
                throw new CustomException("无可用负责人--error:790");
            }
            return byId;
        }
        String[] deptIds = ancestors.split(",");
        if ((deptIds.length + 1) < deep) {
            deep = deptIds.length + 1;
        }
        int currentDeep = 1;
        User zhaoDao = null;
        for (String deptId : deptIds) {
            if (currentDeep >= deep) {
                SysDept byId = iSysDeptService.getById(Long.valueOf(deptId));
                if (byId == null) {
                    continue;
                }
                if (byId.getLeaderId() == null) {
                    continue;
                }
                User user = userDao.getById(byId.getLeaderId());
                if (user == null) {
                    continue;
                }
                zhaoDao = user;
                break;
            }
            currentDeep += 1;
        }
        if (zhaoDao == null) {
            // 再尝试根组织下有没有负责人，有就用!
            LambdaQueryWrapper<SysDept> sysDeptLambdaQueryWrapper = new LambdaQueryWrapper<>();
            sysDeptLambdaQueryWrapper.eq(SysDept::getParentId, 0L);
            SysDept sysDept1 = iSysDeptService.getOne(sysDeptLambdaQueryWrapper);
            if (sysDept1 != null) {
                User user = userDao.getById(sysDept1.getLeaderId());
                if (user != null) {
                    return user;
                }

            }
            throw new CustomException("无可用负责人--error:790");
        }
        return zhaoDao;
    }

}
