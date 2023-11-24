package com.qc.printers.custom.notice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qc.printers.common.common.Code;
import com.qc.printers.common.common.CustomException;
import com.qc.printers.common.common.domain.entity.PageData;
import com.qc.printers.common.common.utils.ThreadLocalUtil;
import com.qc.printers.common.notice.dao.NoticeAnnexDao;
import com.qc.printers.common.notice.dao.NoticeDao;
import com.qc.printers.common.notice.dao.NoticeDeptDao;
import com.qc.printers.common.notice.dao.NoticeUserReadDao;
import com.qc.printers.common.notice.domain.entity.Notice;
import com.qc.printers.common.notice.domain.entity.NoticeAnnex;
import com.qc.printers.common.notice.domain.entity.NoticeDept;
import com.qc.printers.common.notice.domain.entity.NoticeUserRead;
import com.qc.printers.common.notice.service.INoticeAnnexService;
import com.qc.printers.common.user.dao.UserDao;
import com.qc.printers.common.user.domain.dto.UserInfo;
import com.qc.printers.common.user.service.ISysDeptService;
import com.qc.printers.custom.notice.domain.vo.req.NoticeAddReq;
import com.qc.printers.custom.notice.domain.vo.resp.NoticeAddResp;
import com.qc.printers.custom.notice.domain.vo.resp.NoticeMangerListResp;
import com.qc.printers.custom.notice.service.NoticeService;
import com.qc.printers.custom.notice.utils.UpdateUserListUtil;
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
    private UserDao userDao;

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
     * 仅返回必要字段，其余信息不返回，后续需要通过id查询即可
     *
     * @param type       1:仅看我创建的，2:看所有人
     * @param pageNum    分页参数
     * @param pageSize   分页每页大小
     * @param search     搜索key
     * @param searchType 搜索方式:1:通知名，2：tag暂不支持从内容搜索
     * @return
     */
    @Override
    public PageData<NoticeMangerListResp> publishNoticeList(Integer type, Integer status, Integer pageNum, Integer pageSize, String search, Integer searchType) {
        UserInfo currentUser = ThreadLocalUtil.getCurrentUser();
        if (currentUser == null) {
            throw new CustomException("登录信息错误", Code.DEL_TOKEN);
        }
        Page<Notice> pageInfo = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Notice> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        //条件过滤
        if (searchType.equals(2)) {
            lambdaQueryWrapper.like(!StringUtils.isEmpty(search), Notice::getTag, search);
        } else if (searchType.equals(1)) {
            lambdaQueryWrapper.like(!StringUtils.isEmpty(search), Notice::getTitle, search);
        }
        lambdaQueryWrapper.eq(status != null, Notice::getStatus, status);
        lambdaQueryWrapper.orderByDesc(Notice::getCreateTime);
        lambdaQueryWrapper.eq(type.equals(1), Notice::getCreateUser, currentUser.getId());
        noticeDao.page(pageInfo);

        List<NoticeMangerListResp> noticeMangerListResps = new ArrayList<>();
        if (pageInfo.getRecords().size() > 0) {
            for (Notice notice : pageInfo.getRecords()) {
                NoticeMangerListResp noticeMangerListResp = new NoticeMangerListResp();
                noticeMangerListResp.setAmount(notice.getAmount());
                noticeMangerListResp.setTag(notice.getTag());
                noticeMangerListResp.setStatus(notice.getStatus());
                noticeMangerListResp.setId(notice.getId());
                noticeMangerListResp.setTitle(notice.getTitle());
                noticeMangerListResp.setCreateTime(notice.getCreateTime());
                noticeMangerListResp.setUpdateTime(notice.getUpdateTime());
                noticeMangerListResp.setUrgency(notice.getUrgency());
                noticeMangerListResp.setVersion(notice.getVersion());
                noticeMangerListResp.setVisibility(notice.getVisibility());
                noticeMangerListResp.setIsAnnex(notice.getIsAnnex());
                if (notice.getStatus().equals(2)) {
                    noticeMangerListResp.setReleaseUserName(notice.getReleaseUserName());
                    noticeMangerListResp.setReleaseTime(notice.getReleaseTime());
                    noticeMangerListResp.setReleaseDeptName(notice.getReleaseDeptName());
                }
                noticeMangerListResp.setUpdateUserName(userDao.getById(notice.getCreateUser()).getName());
                noticeMangerListResp.setCreateUserName(userDao.getById(notice.getUpdateUser()).getName());
                noticeMangerListResps.add(noticeMangerListResp);
            }
        }
        PageData<NoticeMangerListResp> mangerListRespPageData = new PageData<>();
        mangerListRespPageData.setTotal(pageInfo.getTotal());
        mangerListRespPageData.setPages(pageInfo.getPages());
        mangerListRespPageData.setCountId(pageInfo.getCountId());
        mangerListRespPageData.setCurrent(pageInfo.getCurrent());
        mangerListRespPageData.setSize(pageInfo.getSize());
        mangerListRespPageData.setRecords(noticeMangerListResps);
        mangerListRespPageData.setMaxLimit(pageInfo.getMaxLimit());
        return mangerListRespPageData;

    }

    @Transactional
    @Override
    public void deleteNotice(String noticeId) {
        Long noticeIdL = Long.valueOf(noticeId);
        // 永久删除用户阅读数据，逻辑删除notice
        LambdaQueryWrapper<NoticeUserRead> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(NoticeUserRead::getNoticeId, noticeIdL);
        noticeUserReadDao.remove(lambdaQueryWrapper);
        LambdaQueryWrapper<NoticeDept> noticeDeptLambdaQueryWrapper = new LambdaQueryWrapper<>();
        noticeDeptLambdaQueryWrapper.eq(NoticeDept::getNoticeId, noticeIdL);
        noticeDeptDao.remove(noticeDeptLambdaQueryWrapper);
        LambdaQueryWrapper<NoticeAnnex> noticeAnnexLambdaQueryWrapper = new LambdaQueryWrapper<>();
        noticeAnnexLambdaQueryWrapper.eq(NoticeAnnex::getNoticeId, noticeIdL);
        noticeAnnexDao.remove(noticeAnnexLambdaQueryWrapper);
        noticeDao.removeById(noticeIdL);
    }

    @Transactional
    @Override
    public void updateNoticeBasic(NoticeAddReq noticeAddReq) {
        if (noticeAddReq == null) {
            throw new RuntimeException("通知信息不能为空");
        }
        if (noticeAddReq.getNotice() == null) {
            throw new RuntimeException("通知内容不能为空");
        }
        if (noticeAddReq.getNotice().getId() == null) {
            throw new RuntimeException("通知id不能为空");
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

        Notice notice = noticeDao.getById(noticeAddReq.getNotice().getId());
        notice.setTitle(noticeAddReq.getNotice().getTitle());
        notice.setUrgency(noticeAddReq.getNotice().getUrgency());
        if (StringUtils.isNotEmpty(noticeAddReq.getNotice().getTag())) {
            notice.setTag(noticeAddReq.getNotice().getTag());
        }
        notice.setVisibility(noticeAddReq.getNotice().getVisibility());
        notice.setUpdateUserList(UpdateUserListUtil.getUpdateUserList(notice.getUpdateUserList(), currentUser.getId()));//默认更新用户就是第一个新建的人
        noticeDao.updateById(notice);
        //先删除跟其他部门的绑定

        LambdaQueryWrapper<NoticeDept> noticeDeptLambdaQueryWrapper = new LambdaQueryWrapper<>();
        noticeDeptLambdaQueryWrapper.eq(NoticeDept::getNoticeId, notice.getId());
        noticeDeptDao.remove(noticeDeptLambdaQueryWrapper);
        //再添加新的部门推送
        if (notice.getVisibility().equals(2)) {
            // 选择了部门推送该通知
            for (Long deptId : noticeAddReq.getDeptIds()) {
                NoticeDept noticeDept = new NoticeDept();
                noticeDept.setNoticeId(notice.getId());
                noticeDept.setDeptId(deptId);
                noticeDeptDao.save(noticeDept);

            }
        }
    }


}
