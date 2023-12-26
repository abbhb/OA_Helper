package com.qc.printers.custom.notice.service.strategy.noticeupdate;

import com.qc.printers.common.common.Code;
import com.qc.printers.common.common.CustomException;
import com.qc.printers.common.common.utils.ThreadLocalUtil;
import com.qc.printers.common.notice.dao.NoticeAnnexDao;
import com.qc.printers.common.notice.dao.NoticeDao;
import com.qc.printers.common.notice.dao.NoticeDeptDao;
import com.qc.printers.common.notice.domain.entity.Notice;
import com.qc.printers.common.notice.service.INoticeAnnexService;
import com.qc.printers.common.user.domain.dto.UserInfo;
import com.qc.printers.common.user.service.ISysDeptService;
import com.qc.printers.custom.notice.domain.enums.NoticeStatusEnum;
import com.qc.printers.custom.notice.domain.vo.req.NoticeAddReq;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Slf4j
public class NoticeUpdataPUBLISH extends NoticeUpdateStatusHandel {
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
    NoticeStatusEnum getNoticeUpdateEnum() {
        return NoticeStatusEnum.PUBLISH;
    }

    @Transactional
    @Override
    public String updateNotice(NoticeAddReq noticeBasicReq) {
        if (noticeBasicReq == null) {
            throw new RuntimeException("通知信息不能为空");
        }
        if (noticeBasicReq.getNotice() == null) {
            throw new CustomException("通知信息不能为空");
        }
        if (noticeBasicReq.getNotice().getId() == null) {
            throw new CustomException("通知id不能为空");
        }
        UserInfo currentUser = ThreadLocalUtil.getCurrentUser();
        if (currentUser == null) {
            throw new CustomException("登录信息错误", Code.DEL_TOKEN);
        }
        Notice byId = noticeDao.getById(noticeBasicReq.getNotice().getId());
        if (byId == null) {
            throw new CustomException("通知不存在");
        }
        byId.setStatus(2);
        byId.setReleaseDept(currentUser.getDeptId());
        byId.setReleaseDeptName(iSysDeptService.getById(currentUser.getDeptId()).getDeptName());
        byId.setReleaseUser(currentUser.getId());
        byId.setReleaseUserName(currentUser.getName());
        // 两种情况，是定时发布还是立即发布,此处暂时不抽象
        if (noticeBasicReq.getNotice().getReleaseTime() != null) {
            byId.setReleaseTime(noticeBasicReq.getNotice().getReleaseTime());
        } else {
            byId.setReleaseTime(LocalDateTime.now());
        }
        noticeDao.updateById(byId);
        if (noticeBasicReq.getNotice().getReleaseTime() != null) {
            return "定时成功";
        }
        return "发布成功";
    }
}
