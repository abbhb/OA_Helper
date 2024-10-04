package com.qc.printers.common.print.service.engine;


import com.qc.printers.common.common.utils.AssertUtil;

import java.util.HashMap;
import java.util.Map;

public class FileVerificationEngineFactory {

    private static final Map<String, FileVerificationEngine> STRATEGY_MAP = new HashMap<>();

    public static void register(String code, FileVerificationEngine strategy) {
        STRATEGY_MAP.put(code, strategy);
    }

    public static FileVerificationEngine getStrategyNoNull(String code) {
        FileVerificationEngine strategy = STRATEGY_MAP.get(code);
        AssertUtil.isNotEmpty(strategy, "该文件类型不支持");
        return strategy;
    }
}
