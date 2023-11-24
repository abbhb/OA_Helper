package com.qc.printers.custom.notice.service;

import com.qc.printers.common.common.domain.entity.PageData;
import com.qc.printers.custom.notice.domain.vo.req.NoticeAddReq;
import com.qc.printers.custom.notice.domain.vo.resp.NoticeAddResp;
import com.qc.printers.custom.notice.domain.vo.resp.NoticeMangerListResp;

public interface NoticeService {
    NoticeAddResp addNotice(NoticeAddReq noticeAddReq);

    PageData<NoticeMangerListResp> publishNoticeList(Integer type, Integer status, Integer pageNum, Integer pageSize, String search, Integer searchType);

    void deleteNotice(String noticeId);

    void updateNoticeBasic(NoticeAddReq noticeAddReq);
}
