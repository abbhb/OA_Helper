package com.qc.printers.common.notice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qc.printers.common.notice.dao.NoticeAnnexDao;
import com.qc.printers.common.notice.domain.entity.NoticeAnnex;
import com.qc.printers.common.notice.service.INoticeAnnexService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class INoticeAnnexServiceImpl implements INoticeAnnexService {
    @Autowired
    private NoticeAnnexDao noticeAnnexDao;


    @Transactional
    @Override
    public void deleteByNoticeId(Long noticeId) {
        LambdaQueryWrapper<NoticeAnnex> noticeAnnexLambdaQueryWrapper = new LambdaQueryWrapper<>();
        noticeAnnexLambdaQueryWrapper.eq(NoticeAnnex::getNoticeId, noticeId);
        noticeAnnexDao.remove(noticeAnnexLambdaQueryWrapper);
    }
}
