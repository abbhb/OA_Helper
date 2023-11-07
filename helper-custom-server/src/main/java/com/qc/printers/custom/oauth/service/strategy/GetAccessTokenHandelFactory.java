package com.qc.printers.custom.oauth.service.strategy;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class GetAccessTokenHandelFactory implements ApplicationContextAware {
    private final Map<String, GetAccessTokenHandel> STRATEGY_MAP = new ConcurrentHashMap<>(8);

//    public static void register(String grantType, GetAccessTokenHandel strategy) {
//        STRATEGY_MAP.put(grantType, strategy);
//    }

    public <T extends GetAccessTokenHandel> GetAccessTokenHandel getInstance(String grantType) {
        GetAccessTokenHandel getAccessTokenHandel = STRATEGY_MAP.get(grantType);
        if (getAccessTokenHandel == null) {
            return null;
        }
        return getAccessTokenHandel;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        Map<String, GetAccessTokenHandel> beabs = applicationContext.getBeansOfType(GetAccessTokenHandel.class);
        beabs.values().forEach(getAccessTokenHandel -> STRATEGY_MAP.put(getAccessTokenHandel.getDataTypeEnum().getGrantType(), getAccessTokenHandel));
    }
}
