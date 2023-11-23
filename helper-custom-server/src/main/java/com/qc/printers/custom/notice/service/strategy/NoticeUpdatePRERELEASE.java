package com.qc.printers.custom.notice.service.strategy;

import com.qc.printers.custom.notice.domain.enums.NoticeUpdateEnum;
import com.qc.printers.custom.notice.domain.vo.req.NoticeUpdateReq;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class NoticeUpdatePRERELEASE extends NoticeUpdateHandel {
    @Override
    NoticeUpdateEnum getNoticeUpdateEnum() {
        return NoticeUpdateEnum.PRERELEASE;
    }

    /**
     * 预发布
     * 直接改状态为预发布，然后前端成功后，直接通过id去获取预发布地址，写两个接口，一个获取预发布地址，当id为预发布时生成对应查看接口的地址，查看接口可以配合前端，新建个预发布预览组件，这样更容易拓展
     *
     * @param noticeUpdateReq
     * @return
     */
    @Override
    public String updateNotice(NoticeUpdateReq noticeUpdateReq) {
        return null;

    }
}
