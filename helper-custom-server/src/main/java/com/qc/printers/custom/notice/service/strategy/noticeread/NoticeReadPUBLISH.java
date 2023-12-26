package com.qc.printers.custom.notice.service.strategy.noticeread;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qc.printers.common.common.CustomException;
import com.qc.printers.common.common.utils.ThreadLocalUtil;
import com.qc.printers.common.notice.dao.NoticeDeptDao;
import com.qc.printers.common.notice.dao.NoticeUserReadDao;
import com.qc.printers.common.notice.domain.entity.Notice;
import com.qc.printers.common.notice.domain.entity.NoticeDept;
import com.qc.printers.common.notice.domain.entity.NoticeUserRead;
import com.qc.printers.common.user.domain.dto.UserInfo;
import com.qc.printers.custom.notice.domain.enums.NoticeStatusEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Slf4j
public class NoticeReadPUBLISH extends NoticeReadHandel {

    @Autowired
    private NoticeDeptDao noticeDeptDao;

    @Autowired
    private NoticeUserReadDao noticeUserReadDao;

    @Override
    NoticeStatusEnum getNoticeReadEnum() {
        return NoticeStatusEnum.PUBLISH;
    }

    @Transactional
    @Override
    public void readSuccessLog(Notice notice) {
        /**
         *  此处直接记录而不是用
         *  @see com.qc.printers.custom.notice.service.NoticeService
         *  里的addLog方法是因为这里前面已经判断了可以阅读，肯定符合条件的，没必要重复校验
         */

        NoticeUserRead noticeUserRead = new NoticeUserRead();
        noticeUserRead.setNoticeId(notice.getId());
        UserInfo currentUser = ThreadLocalUtil.getCurrentUser();
        if (currentUser == null) {
            throw new CustomException("无法鉴权,禁止查看");
        }
        noticeUserRead.setUserId(currentUser.getId());
        noticeUserReadDao.save(noticeUserRead);
    }

    @Override
    public boolean canRead(Notice notice, String password) {
        if (notice == null) {
            throw new CustomException("通知不存在");
        }
        if (notice.getReleaseTime().isAfter(LocalDateTime.now())) {
            // 当前该通知还没被发布
            throw new CustomException("通知不存在");
        }
        if (notice.getVisibility().equals(2)) {
            UserInfo currentUser = ThreadLocalUtil.getCurrentUser();
            if (currentUser == null) {
                throw new CustomException("无法鉴权,禁止查看");
            }
            LambdaQueryWrapper<NoticeDept> lambdaQueryWrapper = new LambdaQueryWrapper<>();
            lambdaQueryWrapper.eq(NoticeDept::getNoticeId, notice.getId());
            lambdaQueryWrapper.eq(NoticeDept::getDeptId, currentUser.getDeptId());
            log.info("当前用户部门id：" + currentUser.getDeptId());
            if (noticeDeptDao.count(lambdaQueryWrapper) < 1) {
                throw new CustomException("权限不足，无法查看");
            }
        }
        return true;
    }
}
