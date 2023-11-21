package com.qc.printers.custom.notice.service;

import com.qc.printers.custom.notice.domain.vo.req.NoticeAddReq;
import com.qc.printers.custom.notice.domain.vo.req.NoticeUpdateReq;
import com.qc.printers.custom.notice.domain.vo.resp.NoticeAddResp;

public interface NoticeService {
    NoticeAddResp addNotice(NoticeAddReq noticeAddReq);

    String updateNotice(NoticeUpdateReq noticeUpdateReq);

    void publishNotice(Long id);
}
