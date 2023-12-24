package com.qc.printers.custom.notice.service.strategy;

import com.qc.printers.common.common.utils.AssertUtil;
import com.qc.printers.common.notice.dao.NoticeAnnexDao;
import com.qc.printers.common.notice.dao.NoticeDao;
import com.qc.printers.common.notice.domain.entity.Notice;
import com.qc.printers.common.notice.service.INoticeAnnexService;
import com.qc.printers.custom.notice.domain.enums.NoticeUpdateEnum;
import com.qc.printers.custom.notice.domain.vo.req.NoticeAddReq;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 更新为草稿
 */
@Service
@Slf4j
public class NoticeUpdateDRAFT extends NoticeUpdateStatusHandel {
    @Autowired
    private INoticeAnnexService iNoticeAnnexService;

    @Autowired
    private NoticeDao noticeDao;

    @Autowired
    private NoticeAnnexDao noticeAnnexDao;

    @Override
    NoticeUpdateEnum getNoticeUpdateEnum() {
        return NoticeUpdateEnum.DRAFT;
    }

    @Transactional
    @Override
    public String updateNotice(NoticeAddReq noticeBasicReq) {
        if (noticeBasicReq == null) {
            throw new RuntimeException("通知信息不能为空");
        }
        if (noticeBasicReq.getNotice() == null) {
            throw new RuntimeException("通知内容不能为空");
        }
        if (noticeBasicReq.getNotice().getId() == null) {
            throw new RuntimeException("通知id不能为空");
        }
        Notice noticeDaoById = noticeDao.getById(noticeBasicReq.getNotice().getId());
        AssertUtil.notEqual(noticeDaoById, null, "通知不存在");
        noticeDaoById.setStatus(0);

        noticeDao.updateById(noticeDaoById);
        return "更新内容成功";
    }
}
