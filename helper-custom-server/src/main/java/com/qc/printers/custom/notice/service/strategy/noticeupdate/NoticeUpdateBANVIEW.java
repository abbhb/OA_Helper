package com.qc.printers.custom.notice.service.strategy.noticeupdate;


import com.qc.printers.common.common.Code;
import com.qc.printers.common.common.CustomException;
import com.qc.printers.common.common.utils.ThreadLocalUtil;
import com.qc.printers.common.notice.dao.NoticeAnnexDao;
import com.qc.printers.common.notice.dao.NoticeDao;
import com.qc.printers.common.notice.domain.entity.Notice;
import com.qc.printers.common.notice.service.INoticeAnnexService;
import com.qc.printers.common.user.domain.dto.UserInfo;
import com.qc.printers.custom.notice.domain.enums.NoticeStatusEnum;
import com.qc.printers.custom.notice.domain.vo.req.NoticeAddReq;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class NoticeUpdateBANVIEW extends NoticeUpdateStatusHandel {
    @Autowired
    private INoticeAnnexService iNoticeAnnexService;

    @Autowired
    private NoticeDao noticeDao;

    @Autowired
    private NoticeAnnexDao noticeAnnexDao;

    @Override
    NoticeStatusEnum getNoticeUpdateEnum() {
        return NoticeStatusEnum.BANVIEW;
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
        byId.setStatus(3);
        noticeDao.updateById(byId);
        return "禁止查看成功";
    }
}
