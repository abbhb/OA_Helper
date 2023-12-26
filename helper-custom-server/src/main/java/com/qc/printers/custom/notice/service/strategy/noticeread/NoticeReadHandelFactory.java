package com.qc.printers.custom.notice.service.strategy.noticeread;

import com.qc.printers.common.common.CustomException;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


@Component
public class NoticeReadHandelFactory implements ApplicationContextAware {
    private final Map<Integer, NoticeReadHandel> STRATEGY_MAP = new ConcurrentHashMap<>(8);

//    public static void register(String grantType, GetAccessTokenHandel strategy) {
//        STRATEGY_MAP.put(grantType, strategy);
//    }

    public <T extends NoticeReadHandel> NoticeReadHandel getInstance(Integer status) {
        NoticeReadHandel noticeReadHandel = STRATEGY_MAP.get(status);
        if (noticeReadHandel == null) {
            throw new CustomException("通知不存在");
        }
        return noticeReadHandel;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        Map<String, NoticeReadHandel> beabs = applicationContext.getBeansOfType(NoticeReadHandel.class);
        beabs.values().forEach(noticeReadHandel -> STRATEGY_MAP.put(noticeReadHandel.getNoticeReadEnum().getStatus(), noticeReadHandel));
    }
}
