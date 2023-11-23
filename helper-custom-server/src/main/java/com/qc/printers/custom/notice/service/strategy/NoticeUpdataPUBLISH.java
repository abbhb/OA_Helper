package com.qc.printers.custom.notice.service.strategy;

import com.qc.printers.common.common.Code;
import com.qc.printers.common.common.CustomException;
import com.qc.printers.common.common.utils.ThreadLocalUtil;
import com.qc.printers.common.notice.dao.NoticeAnnexDao;
import com.qc.printers.common.notice.dao.NoticeDao;
import com.qc.printers.common.notice.dao.NoticeDeptDao;
import com.qc.printers.common.notice.domain.entity.Notice;
import com.qc.printers.common.notice.domain.entity.NoticeAnnex;
import com.qc.printers.common.notice.service.INoticeAnnexService;
import com.qc.printers.common.user.domain.dto.UserInfo;
import com.qc.printers.common.user.service.ISysDeptService;
import com.qc.printers.custom.notice.domain.enums.NoticeUpdateEnum;
import com.qc.printers.custom.notice.domain.vo.req.NoticeUpdateReq;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Slf4j
public class NoticeUpdataPUBLISH extends NoticeUpdateHandel {
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

    @Override
    NoticeUpdateEnum getNoticeUpdateEnum() {
        return NoticeUpdateEnum.PUBLISH;
    }

    @Transactional
    @Override
    public String updateNotice(NoticeUpdateReq noticeUpdateReq) {
        if (noticeUpdateReq == null) {
            throw new RuntimeException("通知信息不能为空");
        }
        if (noticeUpdateReq.getNotice() == null) {
            throw new CustomException("通知信息不能为空");
        }
        if (noticeUpdateReq.getNotice().getId() == null) {
            throw new CustomException("通知id不能为空");
        }
        UserInfo currentUser = ThreadLocalUtil.getCurrentUser();
        if (currentUser == null) {
            throw new CustomException("登录信息错误", Code.DEL_TOKEN);
        }
        Notice byId = noticeDao.getById(noticeUpdateReq.getNotice().getId());
        if (byId == null) {
            throw new CustomException("通知不存在");
        }
        byId.setStatus(2);
        byId.setReleaseDept(currentUser.getDeptId());
        byId.setReleaseDeptName(iSysDeptService.getById(currentUser.getDeptId()).getDeptName());
        byId.setReleaseUser(currentUser.getId());
        byId.setReleaseUserName(currentUser.getName());
        byId.setContent(noticeUpdateReq.getNotice().getContent());
        // 两种情况，是定时发布还是立即发布,此处暂时不抽象
        if (noticeUpdateReq.getNotice().getReleaseTime() != null) {
            byId.setReleaseTime(noticeUpdateReq.getNotice().getReleaseTime());
        } else {
            byId.setReleaseTime(LocalDateTime.now());
        }
        if (noticeUpdateReq.getAnnexes() != null) {
            if (noticeUpdateReq.getAnnexes().size() > 0) {
                byId.setIsAnnex(1);
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
        noticeDao.updateById(byId);
        if (noticeUpdateReq.getNotice().getReleaseTime() != null) {
            return "定时成功";
        }
        return "发布成功";
    }
}
