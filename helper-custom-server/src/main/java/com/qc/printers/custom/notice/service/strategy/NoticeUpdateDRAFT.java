package com.qc.printers.custom.notice.service.strategy;

import com.qc.printers.common.common.utils.AssertUtil;
import com.qc.printers.common.notice.dao.NoticeAnnexDao;
import com.qc.printers.common.notice.dao.NoticeDao;
import com.qc.printers.common.notice.domain.entity.Notice;
import com.qc.printers.common.notice.domain.entity.NoticeAnnex;
import com.qc.printers.common.notice.service.INoticeAnnexService;
import com.qc.printers.custom.notice.domain.enums.NoticeUpdateEnum;
import com.qc.printers.custom.notice.domain.vo.req.NoticeUpdateReq;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class NoticeUpdateDRAFT extends NoticeUpdateHandel {
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
    public String updateNotice(NoticeUpdateReq noticeUpdateReq) {
        if (noticeUpdateReq == null) {
            throw new RuntimeException("通知信息不能为空");
        }
        if (noticeUpdateReq.getNotice() == null) {
            throw new RuntimeException("通知内容不能为空");
        }
        if (noticeUpdateReq.getNotice().getId() == null) {
            throw new RuntimeException("通知id不能为空");
        }
        Notice noticeDaoById = noticeDao.getById(noticeUpdateReq.getNotice().getId());
        AssertUtil.notEqual(noticeDaoById, null, "通知不存在");
        noticeDaoById.setContent(noticeUpdateReq.getNotice().getContent());
        if (noticeUpdateReq.getAnnexes() != null) {
            if (noticeUpdateReq.getAnnexes().size() > 0) {
                noticeDaoById.setIsAnnex(1);
                //删掉老附件，再添加新的附件信息
                iNoticeAnnexService.deleteByNoticeId(noticeUpdateReq.getNotice().getId());
                for (NoticeAnnex annex : noticeUpdateReq.getAnnexes()) {
                    NoticeAnnex noticeAnnex = new NoticeAnnex();
                    noticeAnnex.setNoticeId(noticeUpdateReq.getNotice().getId());
                    noticeAnnex.setFileUrl(annex.getFileUrl());
                    noticeAnnex.setDownloadCount(0);
                    noticeAnnex.setSortNum(annex.getSortNum());
                    noticeAnnexDao.save(noticeAnnex);
                }
            }
        }
        noticeDao.updateById(noticeDaoById);
        return "更新内容成功";
    }
}
