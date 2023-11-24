package com.qc.printers.custom.notice.service.strategy;

import com.qc.printers.common.common.Code;
import com.qc.printers.common.common.CustomException;
import com.qc.printers.common.common.utils.ThreadLocalUtil;
import com.qc.printers.common.notice.dao.NoticeAnnexDao;
import com.qc.printers.common.notice.dao.NoticeDao;
import com.qc.printers.common.notice.domain.entity.Notice;
import com.qc.printers.common.notice.domain.entity.NoticeAnnex;
import com.qc.printers.common.notice.service.INoticeAnnexService;
import com.qc.printers.common.user.domain.dto.UserInfo;
import com.qc.printers.custom.notice.domain.enums.NoticeUpdateEnum;
import com.qc.printers.custom.notice.domain.vo.req.NoticeUpdateReq;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class NoticeUpdatePRERELEASE extends NoticeUpdateHandel {

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
     * @param noticeUpdateReq
     * @return
     */
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
        byId.setStatus(1);
        byId.setContent(noticeUpdateReq.getNotice().getContent());
        // 预发布和草稿没啥区别，只是能被预览
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
        return "预发布成功,如预览链接未生成请手动！";

    }
}
