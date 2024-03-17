package com.qc.printers.common.activiti.service.strategy;

import com.qc.printers.common.common.exception.CommonErrorEnum;
import com.qc.printers.common.common.utils.AssertUtil;

import java.util.HashMap;
import java.util.Map;

public class AssigneeLeaderHandelFactory {
    private static final Map<String, AbstractAssigneeLeaderHandel> STRATEGY_MAP = new HashMap<>();

    public static void register(String assigneeLeaderType, AbstractAssigneeLeaderHandel strategy) {
        STRATEGY_MAP.put(assigneeLeaderType, strategy);
    }

    public static AbstractAssigneeLeaderHandel getStrategyNoNull(String assigneeLeaderType) {
        AbstractAssigneeLeaderHandel strategy = STRATEGY_MAP.get(assigneeLeaderType);
        AssertUtil.isNotEmpty(strategy, CommonErrorEnum.PARAM_VALID);
        return strategy;
    }
}
