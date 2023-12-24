package com.qc.printers.custom.notice.service.strategy;

import com.qc.printers.common.notice.dao.NoticeAnnexDao;
import com.qc.printers.common.notice.dao.NoticeDao;
import com.qc.printers.common.notice.dao.NoticeDeptDao;
import com.qc.printers.common.notice.service.INoticeAnnexService;
import com.qc.printers.common.user.service.ISysDeptService;
import com.qc.printers.custom.notice.domain.enums.NoticeUpdateEnum;
import com.qc.printers.custom.notice.domain.vo.req.NoticeAddReq;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 通知更新处理类
 * 仅仅更新状态
 */

@Service
@Slf4j
public abstract class NoticeUpdateStatusHandel {
    @Autowired
    private INoticeAnnexService iNoticeAnnexService;

    @Autowired
    private NoticeDeptDao noticeDeptDao;

    @Autowired
    private NoticeDao noticeDao;

    @Autowired
    private NoticeAnnexDao noticeAnnexDao;

    @Autowired
    private ISysDeptService iSysDeptService;

    /**
     * 数据的类型
     */
    abstract NoticeUpdateEnum getNoticeUpdateEnum();

    /**
     * 生成返回数据
     * 返回数据都基于PrinterBaseResp<T></>
     */
    public abstract String updateNotice(NoticeAddReq noticeBasicReq);

}
