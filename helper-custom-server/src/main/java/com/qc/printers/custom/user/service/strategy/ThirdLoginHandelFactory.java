package com.qc.printers.custom.user.service.strategy;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ThirdLoginHandelFactory implements ApplicationContextAware {
    private final Map<String, ThirdLoginHandel> STRATEGY_MAP = new ConcurrentHashMap<>(8);

    public <T extends ThirdLoginHandel> ThirdLoginHandel getInstance(String type) {
        ThirdLoginHandel thirdLoginHandel = STRATEGY_MAP.get(type);
        if (thirdLoginHandel == null) {
            return null;
        }
        return thirdLoginHandel;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        Map<String, ThirdLoginHandel> beabs = applicationContext.getBeansOfType(ThirdLoginHandel.class);
        beabs.values().forEach(thirdLoginHandel -> STRATEGY_MAP.put(thirdLoginHandel.getDataTypeEnum().getType(), thirdLoginHandel));
    }
}
