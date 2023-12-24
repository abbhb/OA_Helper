package com.qc.printers.custom.notice.service;

import com.qc.printers.common.common.domain.entity.PageData;
import com.qc.printers.custom.notice.domain.vo.req.NoticeAddReq;
import com.qc.printers.custom.notice.domain.vo.req.NoticeUpdateReq;
import com.qc.printers.custom.notice.domain.vo.resp.NoticeAddResp;
import com.qc.printers.custom.notice.domain.vo.resp.NoticeMangerListResp;
import com.qc.printers.custom.notice.domain.vo.resp.NoticeUserResp;
import com.qc.printers.custom.notice.domain.vo.resp.NoticeViewResp;

public interface NoticeService {
    NoticeAddResp addNotice(NoticeAddReq noticeAddReq);

    PageData<NoticeMangerListResp> publishNoticeList(Integer type, Integer status, Integer pageNum, Integer pageSize, String search, Integer searchType);

    void deleteNotice(String noticeId);

    void quickUpdateNotice(NoticeAddReq noticeAddReq);

    NoticeViewResp viewNoticeEdit(String noticeId);

    void updateNoticeContent(NoticeUpdateReq noticeUpdateReq);

    PageData<NoticeUserResp> getNoticeList(Integer urgency, Integer pageNum, Integer pageSize, String tag, Long deptId);
}
