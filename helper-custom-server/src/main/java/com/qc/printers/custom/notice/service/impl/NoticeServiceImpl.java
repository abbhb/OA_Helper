package com.qc.printers.custom.notice.service.impl;

import com.qc.printers.common.common.Code;
import com.qc.printers.common.common.CustomException;
import com.qc.printers.common.common.utils.ThreadLocalUtil;
import com.qc.printers.common.notice.dao.NoticeAnnexDao;
import com.qc.printers.common.notice.dao.NoticeDao;
import com.qc.printers.common.notice.dao.NoticeDeptDao;
import com.qc.printers.common.notice.dao.NoticeUserReadDao;
import com.qc.printers.common.notice.domain.entity.Notice;
import com.qc.printers.common.notice.domain.entity.NoticeDept;
import com.qc.printers.common.notice.service.INoticeAnnexService;
import com.qc.printers.common.user.domain.dto.UserInfo;
import com.qc.printers.common.user.service.ISysDeptService;
import com.qc.printers.custom.notice.domain.vo.req.NoticeAddReq;
import com.qc.printers.custom.notice.domain.vo.resp.NoticeAddResp;
import com.qc.printers.custom.notice.service.NoticeService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class NoticeServiceImpl implements NoticeService {

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

    @Autowired
    private NoticeUserReadDao noticeUserReadDao;


    @Transactional
    @Override
    public NoticeAddResp addNotice(NoticeAddReq noticeAddReq) {
        if (noticeAddReq == null) {
            throw new RuntimeException("通知信息不能为空");
        }
        if (noticeAddReq.getNotice() == null) {
            throw new RuntimeException("通知内容不能为空");
        }
        if (StringUtils.isEmpty(noticeAddReq.getNotice().getTitle())) {
            throw new RuntimeException("通知标题不能为空");
        }
        if (noticeAddReq.getNotice().getUrgency() == null) {
            throw new RuntimeException("通知紧急程度不能为空");
        }
        if (noticeAddReq.getNotice().getVisibility() == null) {
            throw new RuntimeException("可见性参数不能为空");
        }
        if (noticeAddReq.getNotice().getVisibility().equals(2)) {
            if (noticeAddReq.getDeptIds() == null) {
                throw new RuntimeException("通知接收人不能为空");
            }
            if (noticeAddReq.getDeptIds().size() == 0) {
                throw new RuntimeException("通知接收人不能为空");
            }
        }
        UserInfo currentUser = ThreadLocalUtil.getCurrentUser();
        if (currentUser == null) {
            throw new CustomException("登录信息错误", Code.DEL_TOKEN);
        }
        Notice notice = new Notice();
        notice.setTitle(noticeAddReq.getNotice().getTitle());
        notice.setUrgency(noticeAddReq.getNotice().getUrgency());
        notice.setStatus(0);
        notice.setAmount(0);// 默认阅读量为空
        notice.setIsAnnex(0);
        notice.setContent("");
        notice.setVersion(1);
        if (StringUtils.isNotEmpty(noticeAddReq.getNotice().getTag())) {
            notice.setTag(noticeAddReq.getNotice().getTag());
        }
        notice.setVisibility(noticeAddReq.getNotice().getVisibility());
        notice.setUpdateUserList(String.valueOf(currentUser.getId()));//默认更新用户就是第一个新建的人
        noticeDao.save(notice);
        List<NoticeDept> noticeDeptListResp = new ArrayList<>();
        if (notice.getVisibility().equals(2)) {
            // 选择了部门推送该通知
            for (Long deptId : noticeAddReq.getDeptIds()) {
                NoticeDept noticeDept = new NoticeDept();
                noticeDept.setNoticeId(notice.getId());
                noticeDept.setDeptId(deptId);
                noticeDeptDao.save(noticeDept);
                //顺便添加到列表
                noticeDeptListResp.add(noticeDept);
            }
        }
        NoticeAddResp noticeAddResp = new NoticeAddResp();
        noticeAddResp.setNotice(notice);
        if (notice.getVisibility().equals(2)) {
            noticeAddResp.setNoticeDepts(noticeDeptListResp);
        }
        return noticeAddResp;
    }


}
