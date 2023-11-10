package com.qc.printers.common.vailcode.service.strategy;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class VailCodeVerifyHandelFactory implements ApplicationContextAware {
    private final Map<Integer, VailCodeVerifyHandel> STRATEGY_MAP = new ConcurrentHashMap<>(8);

    public <T extends VailCodeVerifyHandel> VailCodeVerifyHandel getInstance(Integer type) {
        VailCodeVerifyHandel getAccessTokenHandel = STRATEGY_MAP.get(type);
        if (getAccessTokenHandel == null) {
            return null;
        }
        return getAccessTokenHandel;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        Map<String, VailCodeVerifyHandel> beabs = applicationContext.getBeansOfType(VailCodeVerifyHandel.class);
        beabs.values().forEach(getAccessTokenHandel -> STRATEGY_MAP.put(getAccessTokenHandel.getVailTypeEnum().getType(), getAccessTokenHandel));
    }
}
