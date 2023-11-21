package com.qc.printers.custom.notice.service.impl;

import com.qc.printers.common.common.Code;
import com.qc.printers.common.common.CustomException;
import com.qc.printers.common.common.utils.AssertUtil;
import com.qc.printers.common.common.utils.ThreadLocalUtil;
import com.qc.printers.common.notice.dao.NoticeAnnexDao;
import com.qc.printers.common.notice.dao.NoticeDao;
import com.qc.printers.common.notice.dao.NoticeDeptDao;
import com.qc.printers.common.notice.dao.NoticeUserReadDao;
import com.qc.printers.common.notice.domain.entity.Notice;
import com.qc.printers.common.notice.domain.entity.NoticeAnnex;
import com.qc.printers.common.notice.domain.entity.NoticeDept;
import com.qc.printers.common.notice.service.INoticeAnnexService;
import com.qc.printers.common.user.domain.dto.UserInfo;
import com.qc.printers.common.user.service.ISysDeptService;
import com.qc.printers.custom.notice.domain.vo.req.NoticeAddReq;
import com.qc.printers.custom.notice.domain.vo.req.NoticeUpdateReq;
import com.qc.printers.custom.notice.domain.vo.resp.NoticeAddResp;
import com.qc.printers.custom.notice.service.NoticeService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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

    /**
     * 此方法不更新通知的基本信息
     *
     * @param noticeUpdateReq
     * @return
     */
    @Transactional
    @Override
    public String updateNotice(NoticeUpdateReq noticeUpdateReq) {
        if (noticeUpdateReq == null) {
            throw new RuntimeException("通知信息不能为空");
        }
        if (noticeUpdateReq.getNotice() == null) {
            throw new RuntimeException("通知内容不能为空");
        }
        if (noticeUpdateReq.getNotice().getId() == null) {
            throw new RuntimeException("通知id不能为空");
        }
        Notice noticeDaoById = noticeDao.getById(noticeUpdateReq.getNotice().getId());
        AssertUtil.notEqual(noticeDaoById, null, "通知不存在");
        noticeDaoById.setContent(noticeUpdateReq.getNotice().getContent());
        if (noticeUpdateReq.getAnnexes() != null) {
            if (noticeUpdateReq.getAnnexes().size() > 0) {
                noticeDaoById.setIsAnnex(1);
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
        noticeDao.updateById(noticeDaoById);
        return "更新内容成功";
    }

    /**
     * 发布通知
     *
     * @param id
     */

    @Transactional
    @Override
    public void publishNotice(Long id) {
        if (id == null) {
            throw new RuntimeException("通知id不能为空");
        }
        Notice byId = noticeDao.getById(id);
        if (byId == null) {
            throw new RuntimeException("通知不存在");
        }
        UserInfo currentUser = ThreadLocalUtil.getCurrentUser();
        if (currentUser == null) {
            throw new CustomException("登录信息错误", Code.DEL_TOKEN);
        }
        LocalDateTime now = LocalDateTime.now();
        //此处无脑设置发布状态即可
        byId.setStatus(2);
        byId.setReleaseDept(currentUser.getDeptId());
        byId.setReleaseDeptName(iSysDeptService.getById(currentUser.getDeptId()).getDeptName());
        byId.setReleaseTime(now);
        byId.setReleaseUser(currentUser.getId());
        byId.setReleaseUserName(currentUser.getName());
        noticeDao.updateById(byId);
    }
}
