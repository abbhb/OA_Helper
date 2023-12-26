package com.qc.printers.custom.notice.service.strategy.noticeread;

import com.qc.printers.common.common.CustomException;
import com.qc.printers.common.common.MyString;
import com.qc.printers.common.common.utils.RedisUtils;
import com.qc.printers.common.notice.domain.dto.NoticeReadTime;
import com.qc.printers.common.notice.domain.entity.Notice;
import com.qc.printers.custom.notice.domain.enums.NoticeStatusEnum;
import org.apache.commons.lang.StringUtils;

public class NoticeReadPRERELEASE extends NoticeReadHandel {

    @Override
    NoticeStatusEnum getNoticeReadEnum() {
        return NoticeStatusEnum.PRERELEASE;
    }

    @Override
    public void readSuccessLog(Notice notice) {
        //预发布不需要
    }

    @Override
    public boolean canRead(Notice notice, String password) {
        if (notice == null) {
            throw new CustomException("通知不存在");
        }
        if (StringUtils.isEmpty(password)) {
            throw new CustomException("通知当前为预发布！密码错误");
        }
        if (!RedisUtils.hasKey(MyString.notice_time_read_key + notice.getId())) {
            throw new CustomException("通知当前为预发布！密码错误");
        }
        NoticeReadTime noticeReadTime = RedisUtils.get(MyString.notice_time_read_key + notice.getId(), NoticeReadTime.class);
        if (noticeReadTime == null) {
            throw new CustomException("通知当前为预发布！密码错误");
        }
        if (!noticeReadTime.getPassword().equals(password)) {
            throw new CustomException("通知当前为预发布！密码错误");
        }
        return true;
    }
}
