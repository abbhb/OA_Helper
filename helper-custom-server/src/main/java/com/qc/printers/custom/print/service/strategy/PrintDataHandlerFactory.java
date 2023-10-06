package com.qc.printers.custom.print.service.strategy;

import com.qc.printers.common.common.exception.CommonErrorEnum;
import com.qc.printers.common.common.utils.AssertUtil;

import java.util.HashMap;
import java.util.Map;

public class PrintDataHandlerFactory {
    private static final Map<Integer, AbstratePrintDataHandler> STRATEGY_MAP = new HashMap<>();

    public static void register(Integer code, AbstratePrintDataHandler strategy) {
        STRATEGY_MAP.put(code, strategy);
    }

    public static AbstratePrintDataHandler getStrategyNoNull(Integer code) {
        AbstratePrintDataHandler strategy = STRATEGY_MAP.get(code);
        AssertUtil.isNotEmpty(strategy, CommonErrorEnum.PARAM_VALID);
        return strategy;
    }
}
