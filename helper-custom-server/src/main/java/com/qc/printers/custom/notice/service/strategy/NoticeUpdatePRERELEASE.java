package com.qc.printers.custom.notice.service.strategy;

import com.qc.printers.common.common.Code;
import com.qc.printers.common.common.CustomException;
import com.qc.printers.common.common.utils.ThreadLocalUtil;
import com.qc.printers.common.notice.dao.NoticeAnnexDao;
import com.qc.printers.common.notice.dao.NoticeDao;
import com.qc.printers.common.notice.domain.entity.Notice;
import com.qc.printers.common.notice.service.INoticeAnnexService;
import com.qc.printers.common.user.domain.dto.UserInfo;
import com.qc.printers.custom.notice.domain.enums.NoticeUpdateEnum;
import com.qc.printers.custom.notice.domain.vo.req.NoticeAddReq;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class NoticeUpdatePRERELEASE extends NoticeUpdateStatusHandel {

    @Autowired
    private INoticeAnnexService iNoticeAnnexService;

    @Autowired
    private NoticeDao noticeDao;

    @Autowired
    private NoticeAnnexDao noticeAnnexDao;

    @Override
    NoticeUpdateEnum getNoticeUpdateEnum() {
        return NoticeUpdateEnum.PRERELEASE;
    }

    /**
     * 预发布
     * 直接改状态为预发布，然后前端成功后，直接通过id去获取预发布地址，写两个接口，一个获取预发布地址，当id为预发布时生成对应查看接口的地址，查看接口可以配合前端，新建个预发布预览组件，这样更容易拓展
     * 前端只需要在预发布成功时主动请求预发布地址获取就行
     *
     * @param noticeBasicReq
     * @return
     */
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
        byId.setStatus(1);

        noticeDao.updateById(byId);
        return "预发布成功,如预览链接未生成请手动！";

    }
}
