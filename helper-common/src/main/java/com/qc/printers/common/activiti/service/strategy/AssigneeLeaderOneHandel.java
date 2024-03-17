package com.qc.printers.common.activiti.service.strategy;

import com.qc.printers.common.activiti.constant.Constants;
import com.qc.printers.common.user.dao.UserDao;
import com.qc.printers.common.user.domain.entity.SysDept;
import com.qc.printers.common.user.domain.entity.User;
import com.qc.printers.common.user.service.ISysDeptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class AssigneeLeaderOneHandel extends AbstractAssigneeLeaderHandel {
    @Autowired
    private UserDao userDao;

    @Autowired
    private ISysDeptService iSysDeptService;

    @Override
    String getAssigneeLeaderType() {
        return Constants.PROCESS_ASSIGNEELEADER_1;
    }

    @Override
    public void addVariables(Map<String, Object> variables, String userId) {
        SysDept sysDept = getSysDeptByUser(userId);
        User notNullLeader = getNotNullLeader(sysDept, 2);
        variables.put(getAssigneeLeaderType(), notNullLeader.getId());
        variables.put(getAssigneeLeaderType() + "Name", notNullLeader.getName());
    }
}
