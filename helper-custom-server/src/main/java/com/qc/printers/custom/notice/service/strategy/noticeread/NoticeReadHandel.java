package com.qc.printers.custom.notice.service.strategy.noticeread;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qc.printers.common.common.CustomException;
import com.qc.printers.common.notice.dao.NoticeAnnexDao;
import com.qc.printers.common.notice.dao.NoticeDeptDao;
import com.qc.printers.common.notice.dao.NoticeUserReadDao;
import com.qc.printers.common.notice.domain.entity.Notice;
import com.qc.printers.common.notice.domain.entity.NoticeAnnex;
import com.qc.printers.custom.notice.domain.enums.NoticeStatusEnum;
import com.qc.printers.custom.notice.domain.vo.resp.NoticeUserReadResp;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public abstract class NoticeReadHandel {

    @Autowired
    private NoticeAnnexDao noticeAnnexDao;

    @Autowired
    private NoticeDeptDao noticeDeptDao;

    @Autowired
    private NoticeUserReadDao noticeUserReadDao;

    /**
     * 数据的类型
     */
    abstract NoticeStatusEnum getNoticeReadEnum();

    /**
     * 生成返回数据
     * 返回数据都基于PrinterBaseResp<T></>
     */
    public NoticeUserReadResp readNotice(Notice notice, String password) {
        if (!canRead(notice, password)) {
            throw new CustomException("该通知不能查看");
        }
        // 通用返回逻辑
        NoticeUserReadResp noticeUserReadResp = new NoticeUserReadResp();
        noticeUserReadResp.setNotice(notice);
        LambdaQueryWrapper<NoticeAnnex> noticeAnnexLambdaQueryWrapper = new LambdaQueryWrapper<>();
        noticeAnnexLambdaQueryWrapper.eq(NoticeAnnex::getNoticeId, notice.getId());
        List<NoticeAnnex> list = noticeAnnexDao.list(noticeAnnexLambdaQueryWrapper);
        noticeUserReadResp.setNoticeAnnexes(list);
        readSuccessLog(notice);
        return noticeUserReadResp;
    }

    /**
     * 正常阅读成功就加一条阅读记录,预发布和发布不一样
     *
     * @param notice
     */
    protected abstract void readSuccessLog(Notice notice);

    protected abstract boolean canRead(Notice notice, String password);

}
