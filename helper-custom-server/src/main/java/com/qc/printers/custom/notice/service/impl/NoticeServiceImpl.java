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
import com.qc.printers.custom.notice.domain.vo.req.NoticeAddReq;
import com.qc.printers.custom.notice.domain.vo.req.NoticeUpdateReq;
import com.qc.printers.custom.notice.domain.vo.resp.*;
import com.qc.printers.custom.notice.service.NoticeService;
import com.qc.printers.custom.notice.service.strategy.noticeread.NoticeReadHandelFactory;
import com.qc.printers.custom.notice.service.strategy.noticeupdate.NoticeUpdateHandelFactory;
import com.qc.printers.custom.notice.utils.UpdateUserListUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
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
    private UserDao userDao;

    @Autowired
    private NoticeAnnexDao noticeAnnexDao;

    @Autowired
    private NoticeUserReadDao noticeUserReadDao;
    @Autowired
    private NoticeUpdateHandelFactory noticeUpdateHandelFactory;

    @Autowired
    private NoticeReadHandelFactory noticeReadHandelFactory;

    @Transactional
    @Override
    public NoticeAddResp addNotice(NoticeAddReq noticeAddReq) {
        if (noticeAddReq == null) {
            throw new CustomException("通知信息不能为空");
        }
        if (noticeAddReq.getNotice() == null) {
            throw new CustomException("通知内容不能为空");
        }
        if (StringUtils.isEmpty(noticeAddReq.getNotice().getTitle())) {
            throw new CustomException("通知标题不能为空");
        }
        if (noticeAddReq.getNotice().getUrgency() == null) {
            throw new CustomException("通知紧急程度不能为空");
        }
        if (StringUtils.isEmpty(noticeAddReq.getNotice().getTag())) {
            throw new CustomException("通知Tag不能为空");
        }
        if (noticeAddReq.getNotice().getVisibility() == null) {
            throw new CustomException("可见性参数不能为空");
        }
        if (noticeAddReq.getNotice().getType() == null) {
            throw new CustomException("通知类型不能为空");
        }
        if (noticeAddReq.getNotice().getVisibility().equals(2)) {
            if (noticeAddReq.getDeptIds() == null) {
                throw new CustomException("通知接收人不能为空");
            }
            if (noticeAddReq.getDeptIds().size() == 0) {
                throw new CustomException("通知接收人不能为空");
            }
        }
        if (noticeAddReq.getNotice().getType().equals(2)) {
            if (StringUtils.isEmpty(noticeAddReq.getNotice().getContent())) {
                throw new CustomException("通知跳转链接不可为空");
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
        notice.setType(noticeAddReq.getNotice().getType());
        notice.setAmount(0);// 默认阅读量为空
        notice.setIsAnnex(0);
        //默认通知内容添加为空，但要是url模式可能是有内容的
        notice.setContent("");
        if (noticeAddReq.getNotice().getType().equals(2)) {
            notice.setContent(noticeAddReq.getNotice().getContent());
        }
        notice.setType(noticeAddReq.getNotice().getType());
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
                noticeMangerListResp.setType(notice.getType());
                noticeMangerListResp.setContent(notice.getContent());
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
                if (notice.getVisibility().equals(2)) {
                    List<Long> collect = noticeDeptDao.list(new LambdaQueryWrapper<NoticeDept>().eq(NoticeDept::getNoticeId, notice.getId())).stream().map(NoticeDept::getDeptId).toList();
                    noticeMangerListResp.setDeptIds(collect);

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


    /**
     * 该方法不同的是，如果是更新基本信息，则只更新基本信息，不会涉及到发布状态，此方法多了发布状态的更新
     *
     * @param noticeAddReq
     */
    @Transactional
    @Override
    public void quickUpdateNotice(NoticeAddReq noticeAddReq) {
        if (noticeAddReq == null) {
            throw new CustomException("通知信息不能为空");
        }
        if (noticeAddReq.getNotice() == null) {
            throw new CustomException("通知内容不能为空");
        }
        if (noticeAddReq.getNotice().getId() == null) {
            throw new CustomException("通知id不能为空");
        }
        if (StringUtils.isEmpty(noticeAddReq.getNotice().getTitle())) {
            throw new CustomException("通知标题不能为空");
        }
        if (noticeAddReq.getNotice().getUrgency() == null) {
            throw new CustomException("通知紧急程度不能为空");
        }
        if (noticeAddReq.getNotice().getVisibility() == null) {
            throw new CustomException("可见性参数不能为空");
        }
        if (noticeAddReq.getNotice().getVisibility().equals(2)) {
            if (noticeAddReq.getDeptIds() == null) {
                throw new CustomException("通知接收人不能为空");
            }
            if (noticeAddReq.getDeptIds().size() == 0) {
                throw new CustomException("通知接收人不能为空");
            }
        }
        if (noticeAddReq.getNotice().getType() == null) {
            throw new CustomException("通知类型不能为空");
        }
        if (StringUtils.isEmpty(noticeAddReq.getNotice().getTag())) {
            throw new CustomException("通知Tag不能为空");
        }
        if (noticeAddReq.getNotice().getType().equals(2)) {
            if (StringUtils.isEmpty(noticeAddReq.getNotice().getContent())) {
                throw new CustomException("通知跳转链接不可为空");
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
        notice.setType(noticeAddReq.getNotice().getType());
        if (noticeAddReq.getNotice().getType().equals(2)) {
            // 默认快捷更新无法更新内容，但是url模型下此处为URL链接
            notice.setContent(noticeAddReq.getNotice().getContent());
            // 删除所有的附件
            LambdaQueryWrapper<NoticeAnnex> noticeAnnexLambdaQueryWrapper = new LambdaQueryWrapper<>();
            noticeAnnexLambdaQueryWrapper.eq(NoticeAnnex::getNoticeId, notice.getId());
            noticeAnnexDao.remove(noticeAnnexLambdaQueryWrapper);
        }
        // 更新发布状态
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
        // 更新状态
        String s = noticeUpdateHandelFactory.getInstance(noticeAddReq.getNotice().getStatus()).updateNotice(noticeAddReq);


    }

    @Override
    public NoticeViewResp viewNoticeEdit(String noticeId) {
        if (StringUtils.isEmpty(noticeId)) {
            throw new CustomException("异常，请刷新重试!");
        }
        NoticeViewResp noticeViewResp = new NoticeViewResp();
        noticeViewResp.setNoticeAnnexes(new ArrayList<>());
        noticeViewResp.setNoticeDepts(new ArrayList<>());
        Notice notice = noticeDao.getById(noticeId);
        if (notice == null) {
            throw new CustomException("Null");
        }
        noticeViewResp.setNotice(notice);
        if (notice.getVisibility().equals(2)) {
            // 选择了部门推送该通知
            List<NoticeDept> noticeDepts = noticeDeptDao.list(new LambdaQueryWrapper<NoticeDept>().eq(NoticeDept::getNoticeId, noticeId));
            noticeViewResp.setNoticeDepts(noticeDepts);
        }
        if (notice.getIsAnnex().equals(1)) {
            // 存在附件
            List<NoticeAnnex> list = noticeAnnexDao.list(new LambdaQueryWrapper<NoticeAnnex>().eq(NoticeAnnex::getNoticeId, noticeId));
            if (list.size() > 0) {
                noticeViewResp.setNoticeAnnexes(list);
            }
        }
        return noticeViewResp;
    }

    /**
     * 更新内容和附件
     *
     * @param noticeUpdateReq
     */
    @Transactional
    @Override
    public void updateNoticeContent(NoticeUpdateReq noticeUpdateReq) {
        if (noticeUpdateReq == null) {
            throw new RuntimeException("通知信息不能为空");
        }
        if (noticeUpdateReq.getNotice() == null) {
            throw new RuntimeException("通知内容不能为空");
        }
        if (noticeUpdateReq.getNotice().getId() == null) {
            throw new RuntimeException("通知id不能为空");
        }
        //删掉老附件，再添加新的附件信息
        iNoticeAnnexService.deleteByNoticeId(noticeUpdateReq.getNotice().getId());
        Notice byId = noticeDao.getById(noticeUpdateReq.getNotice().getId());
        byId.setIsAnnex(0);
        byId.setContent(noticeUpdateReq.getNotice().getContent());
        if (noticeUpdateReq.getAnnexes() != null) {
            if (noticeUpdateReq.getAnnexes().size() > 0) {
                byId.setIsAnnex(1);
                for (NoticeAnnex annex : noticeUpdateReq.getAnnexes()) {
                    NoticeAnnex noticeAnnex = new NoticeAnnex();
                    noticeAnnex.setNoticeId(noticeUpdateReq.getNotice().getId());
                    noticeAnnex.setFileUrl(annex.getFileUrl());
                    noticeAnnex.setFileName(annex.getFileName());
                    noticeAnnex.setDownloadCount(0);
                    noticeAnnex.setSortNum(annex.getSortNum());
                    noticeAnnexDao.save(noticeAnnex);
                }
            }
        }
        noticeDao.updateById(byId);
    }

    /**
     * 获取通知列表
     *
     * @param urgency  0为全部紧急程度的，1为一般，2为不急，3为紧急
     * @param pageNum  分页器参数之当前页
     * @param pageSize 分页器参数之每页条数
     * @param tag      暂不完善该功能
     * @param deptId   只显示某发布部门，不为null时
     * @return
     */
    @Override
    public PageData<NoticeUserResp> getNoticeList(Integer urgency, Integer pageNum, Integer pageSize, String tag, Long deptId) {
        Page<Notice> page = new Page<Notice>(pageNum, pageSize);
        LambdaQueryWrapper<Notice> noticeLambdaQueryWrapper = new LambdaQueryWrapper<>();
        if (!urgency.equals(0)) {
            noticeLambdaQueryWrapper.eq(Notice::getUrgency, urgency);
        }
        if (deptId != null) {
            noticeLambdaQueryWrapper.eq(Notice::getReleaseDept, deptId);
        }
        // 此列表只展示发布状态的通知
        noticeLambdaQueryWrapper.eq(Notice::getStatus, 2);
        // 发布时间小于等于当前时间的才会被展示
        noticeLambdaQueryWrapper.le(Notice::getReleaseTime, LocalDateTime.now());

        // 当前用户属于哪个部门
        UserInfo currentUser = ThreadLocalUtil.getCurrentUser();
        Long currentUserDeptId = currentUser.getDeptId();
        if (currentUserDeptId == null) {
            throw new CustomException("未鉴权！禁止查看通知");
        }
        // 只展示全部成员可见的或者自己有权限看到的
        noticeLambdaQueryWrapper.eq(Notice::getVisibility, 1).or().exists("SELECT 1 FROM notice_dept nu WHERE nu.notice_id = notice.id AND nu.is_deleted = 0 AND nu.dept_id = " + String.valueOf(currentUserDeptId));
        noticeLambdaQueryWrapper.orderByDesc(Notice::getReleaseTime);
        noticeDao.page(page, noticeLambdaQueryWrapper);
        List<Notice> records = page.getRecords();
        List<NoticeUserResp> noticeUserRespList = new ArrayList<>();
        for (Notice notice : records) {
            NoticeUserResp noticeUserResp = new NoticeUserResp();
            BeanUtils.copyProperties(notice, noticeUserResp);

            LambdaQueryWrapper<NoticeUserRead> noticeUserReadLambdaQueryWrapper = new LambdaQueryWrapper<>();
            noticeUserReadLambdaQueryWrapper.eq(NoticeUserRead::getNoticeId, notice.getId());
            noticeUserReadLambdaQueryWrapper.eq(NoticeUserRead::getUserId, currentUser.getId());
            noticeUserResp.setUserRead(false);
            if (noticeUserReadDao.count(noticeUserReadLambdaQueryWrapper) > 0) {
                noticeUserResp.setUserRead(true);
            }
            noticeUserRespList.add(noticeUserResp);
        }
        PageData<NoticeUserResp> pageData = new PageData<>();
        pageData.setPages(page.getPages());
        pageData.setSize(page.getSize());
        pageData.setTotal(page.getTotal());
        pageData.setCurrent(page.getCurrent());
        pageData.setMaxLimit(page.getMaxLimit());
        pageData.setCountId(page.getCountId());
        pageData.setRecords(noticeUserRespList);
        return pageData;
    }

    /**
     * 查看发布的或者预发布的通知
     *
     * @param noticeId 查看的通知id
     * @param password 仅在查看预发布的通知需要该字段
     * @return
     */
    @Transactional
    @Override
    public NoticeUserReadResp getNotice(Long noticeId, String password) {
        Notice notice = noticeDao.getById(noticeId);
        if (!notice.getType().equals(1)) {
            throw new CustomException("暂不支持预览该通知!");
        }
        return noticeReadHandelFactory.getInstance(notice.getStatus()).readNotice(notice, password);
    }

    @Transactional
    @Override
    public void addNoticeReadLog(Long noticeId) {
        Notice notice = noticeDao.getById(noticeId);
        if (notice == null) {
            throw new CustomException("阅读失败");
        }
        if (!notice.getStatus().equals(2)) {
            throw new CustomException("阅读失败");
        }
        if (notice.getReleaseTime().isAfter(LocalDateTime.now())) {
            throw new CustomException("阅读失败");
        }
        UserInfo currentUser = ThreadLocalUtil.getCurrentUser();
        if (currentUser == null) {
            throw new CustomException("无法鉴权,阅读失败");
        }
        if (notice.getVisibility().equals(2)) {
            LambdaQueryWrapper<NoticeDept> noticeDeptLambdaQueryWrapper = new LambdaQueryWrapper<>();
            noticeDeptLambdaQueryWrapper.eq(NoticeDept::getNoticeId, notice.getId());
            noticeDeptLambdaQueryWrapper.eq(NoticeDept::getDeptId, currentUser.getDeptId());
            if (noticeDeptDao.count(noticeDeptLambdaQueryWrapper) < 1) {
                throw new CustomException("权限不足,阅读失败");
            }
        }
        NoticeUserRead noticeUserRead = new NoticeUserRead();
        noticeUserRead.setNoticeId(notice.getId());
        noticeUserRead.setUserId(currentUser.getId());
        noticeUserReadDao.save(noticeUserRead);
    }


}
