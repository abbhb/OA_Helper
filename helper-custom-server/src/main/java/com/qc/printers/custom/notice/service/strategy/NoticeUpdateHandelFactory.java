package com.qc.printers.custom.notice.service.strategy;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class NoticeUpdateHandelFactory implements ApplicationContextAware {
    private final Map<Integer, NoticeUpdateHandel> STRATEGY_MAP = new ConcurrentHashMap<>(8);

//    public static void register(String grantType, GetAccessTokenHandel strategy) {
//        STRATEGY_MAP.put(grantType, strategy);
//    }

    public <T extends NoticeUpdateHandel> NoticeUpdateHandel getInstance(Integer status) {
        NoticeUpdateHandel noticeUpdateHandel = STRATEGY_MAP.get(status);
        if (noticeUpdateHandel == null) {
            return null;
        }
        return noticeUpdateHandel;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        Map<String, NoticeUpdateHandel> beabs = applicationContext.getBeansOfType(NoticeUpdateHandel.class);
        beabs.values().forEach(noticeUpdateHandel -> STRATEGY_MAP.put(noticeUpdateHandel.getNoticeUpdateEnum().getStatus(), noticeUpdateHandel));
    }
}
