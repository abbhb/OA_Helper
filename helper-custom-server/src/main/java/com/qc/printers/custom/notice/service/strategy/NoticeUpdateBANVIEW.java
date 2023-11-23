package com.qc.printers.custom.notice.service.strategy;


import com.qc.printers.custom.notice.domain.enums.NoticeUpdateEnum;
import com.qc.printers.custom.notice.domain.vo.req.NoticeUpdateReq;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class NoticeUpdateBANVIEW extends NoticeUpdateHandel {
    @Override
    NoticeUpdateEnum getNoticeUpdateEnum() {
        return NoticeUpdateEnum.BANVIEW;
    }

    @Transactional
    @Override
    public String updateNotice(NoticeUpdateReq noticeUpdateReq) {
        return null;
    }
}
