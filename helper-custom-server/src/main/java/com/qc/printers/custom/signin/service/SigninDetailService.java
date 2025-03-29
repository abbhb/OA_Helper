package com.qc.printers.custom.signin.service;

import com.qc.printers.common.common.CustomException;
import com.qc.printers.common.common.utils.oss.OssDBUtil;
import com.qc.printers.common.signin.dao.SigninLogAskLeaveDao;
import com.qc.printers.common.signin.dao.SigninRenewalDao;
import com.qc.printers.common.signin.domain.dto.SigninLogCliBcDto;
import com.qc.printers.common.signin.domain.dto.SigninLogCliBcItem;
import com.qc.printers.common.signin.domain.entity.SigninLogAskLeave;
import com.qc.printers.common.signin.domain.entity.SigninRenewal;
import com.qc.printers.common.signin.domain.resp.SigninLogForSelfResp;
import com.qc.printers.common.signin.service.SigninLogService;
import com.qc.printers.common.user.dao.UserDao;
import com.qc.printers.common.user.domain.entity.SysDept;
import com.qc.printers.common.user.domain.entity.User;
import com.qc.printers.common.user.service.ISysDeptService;
import com.qc.printers.custom.signin.domain.vo.SigninDetailDataResp;
import com.qc.printers.custom.signin.domain.vo.SigninDetailSigninInfoResp;
import com.qc.printers.custom.signin.domain.vo.SigninDetailUserInfoResp;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
public class SigninDetailService {
    @Autowired
    private UserDao userDao;

    @Autowired
    private ISysDeptService iSysDeptService;

    @Autowired
    private SigninRenewalDao signinRenewalDao;
    @Autowired
    private SigninLogService signinLogService;

    @Autowired
    private SigninLogAskLeaveDao signinLogAskLeaveDao;

    public SigninDetailUserInfoResp getUserInfo(Long userId) {
        User user = userDao.getById(userId);
        if (user==null){
            throw new CustomException("用户不存在");
        }
        SysDept sysDept = iSysDeptService.getById(user.getDeptId());
        if (sysDept==null){
            throw new CustomException("部门不存在");
        }
        SigninDetailUserInfoResp signinDetailUserInfoResp = new SigninDetailUserInfoResp();
        signinDetailUserInfoResp.setName(user.getName());
        signinDetailUserInfoResp.setAvatar(OssDBUtil.toUseUrl(user.getAvatar()));
        signinDetailUserInfoResp.setDepartment(sysDept.getDeptNameAll());
        return signinDetailUserInfoResp;
    }

    public SigninDetailDataResp getSigninInfo(Long userId, String date) {
        SigninLogForSelfResp userDaySelf = signinLogService.getUserDaySelf(userId, LocalDate.parse(date));
        if (userDaySelf == null){
            throw new CustomException("考勤日对象为空");
        }
        // 获取该天，该人全部班次的补签状态，按优先级决断出最终补签状态
        List<SigninLogCliBcDto> bcDetail = userDaySelf.getBcDetail();
        int bqStatusInt = getBqStatusInt(bcDetail);
        String remark = "";
        if (userDaySelf.getState().equals(4)){
            // 请假，需要获取详细请假分类和时间
            if (bcDetail!=null&& !bcDetail.isEmpty()){
                Set<Long> askLeaveIds = new HashSet<>();
                // 班次不为空才有意义
                for (SigninLogCliBcDto bcItem : bcDetail) {
                    List<Long> askLeaveId = bcItem.getAskLeaveId();
                    if (askLeaveId == null){
                        continue;
                    }
                    askLeaveIds.addAll(askLeaveId);
                }
                // 请假id列表为空
                if (!askLeaveIds.isEmpty()) {
                    List<SigninLogAskLeave> signinLogAskLeaveList = signinLogAskLeaveDao.listByIds(askLeaveIds);
                    if (signinLogAskLeaveList != null && !signinLogAskLeaveList.isEmpty()){
                        // 请假信息查询非空
                        StringBuilder sb = new StringBuilder();
                        for (SigninLogAskLeave signinLogAskLeave : signinLogAskLeaveList) {
                            sb.append(signinLogAskLeave.getAskLeaveLeaveType()).append(" ").append(signinLogAskLeave.getStartTime()).append(" - ").append(signinLogAskLeave.getEndTime()).append(";");
                        }
                        remark = sb.toString();
                    }

                }
            }
        }
        return SigninDetailDataResp.builder()
                .date(date + " " + userDaySelf.getCurrentXQ())
                .workingHours(userDaySelf.getWorkingHours())
                .attendanceStatus(convertStateToText(userDaySelf.getState()))
                .supplementStatus(convertBQStateToText(bqStatusInt))
                .durationOfAbsence(userDaySelf.getQueQinTime())
                .remarks(remark)
                .build();
    }


    // 获取 打卡信息（SigninDetailSigninInfoResp） 的数据
    public List<SigninDetailSigninInfoResp> getSigninDetailSigninInfos(Long userId, String date) {
        SigninLogForSelfResp userDaySelf = signinLogService.getUserDaySelf(userId, LocalDate.parse(date));
        if (userDaySelf == null) {
            return new ArrayList<>();
        }
        List<SigninDetailSigninInfoResp> signinDetailSigninInfoResps = new ArrayList<>();
        List<SigninLogCliBcDto> bcDetail = userDaySelf.getBcDetail();
        List<SigninLogCliBcItem> bcItems = new ArrayList<>();
        if (bcDetail != null && !bcDetail.isEmpty()) {
            for (SigninLogCliBcDto bcItem : bcDetail) {
                SigninLogCliBcItem sbItem = bcItem.getSbItem();
                if (sbItem != null) {
                    // 上班打卡
                    bcItems.add(sbItem);
                }
                SigninLogCliBcItem xbItem = bcItem.getXbItem();
                if (xbItem != null) {
                    // 下班打卡
                    bcItems.add(xbItem);
                }
            }
        }
        int index = 0;
        for (SigninLogCliBcItem bcItem : bcItems) {
            SigninDetailSigninInfoResp signinDetailSigninInfoResp = SigninDetailSigninInfoResp.builder()
                    .index(++index)
                    .scheduledPunchTime(String.valueOf(bcItem.getTimeY()))
                    .actualPunchTime(String.valueOf(bcItem.getTimeS()))
                    .absentDuration(String.valueOf(bcItem.getQueQingTime())+" 分钟")
                    .attendanceStatus(convertStateToText(bcItem.getState()))
                    .build();
            // 未打卡
            if (bcItem.getTimeS() == null){
                signinDetailSigninInfoResp.setActualPunchTime("未打卡");
            }
            if (bcItem.getQueQingTime() == null){
                signinDetailSigninInfoResp.setActualPunchTime("-");
            }
            signinDetailSigninInfoResps.add(signinDetailSigninInfoResp);
            if (bcItem.getBq()&&bcItem.getBqId()!=null){
                // 存在补签
                Long bqId = bcItem.getBqId();
                SigninRenewal signinRenewal = signinRenewalDao.getById(bqId);
                if (signinRenewal == null){
                    log.error("补签记录不存在,bq-id:{}",bqId);
                    continue;
                }

                signinDetailSigninInfoResp.setSupplementPoint(String.valueOf(signinRenewal.getRenewalTime()));
                // 补签状态
                if (signinRenewal.getState() == 2){
                    signinDetailSigninInfoResp.setSupplementStatus("已拒绝");
                }else if (signinRenewal.getState() == 0){
                    signinDetailSigninInfoResp.setSupplementStatus("审批中");
                }else if (signinRenewal.getState() == 1){
                    signinDetailSigninInfoResp.setSupplementStatus("已通过");
                }
                signinDetailSigninInfoResp.setSupplementReason(signinRenewal.getRenewalReason());
                signinDetailSigninInfoResp.setSupplementApplyTime(String.valueOf(signinRenewal.getCreateTime()));
                if (signinRenewal.getApprovalTime() != null){
                    signinDetailSigninInfoResp.setSupplementApprovalTime(String.valueOf(signinRenewal.getApprovalTime()));
                }
            }
        }

        return signinDetailSigninInfoResps;
    }

    // 将补签状态转换为文字
    private String convertBQStateToText(int bqState) {
        return switch (bqState) {
            case 0 -> "流程中";
            case 1 -> "通过";
            case 2 -> "拒绝";
            case -1 -> "无补签";
            default -> "未知";
        };
    }

    // 计算整体补签状态
    private int getBqStatusInt(List<SigninLogCliBcDto> bcDetail) {
        // 补签状态
        int bqStatusInt = -1;
        if (bcDetail !=null&& !bcDetail.isEmpty()){
            // 1通过，0流程中，2失败
            for (SigninLogCliBcDto bcItem : bcDetail) {
                SigninLogCliBcItem sbItem = bcItem.getSbItem();
                if (sbItem== null){
                    log.error("上班班次为空,bc-count:{},userid:{}",bcItem.getBcCount(),bcItem.getUserId());
                    continue;
                }
                SigninLogCliBcItem xbItem = bcItem.getXbItem();
                if (xbItem== null){
                    log.error("下班班次为空,bc-count:{},userid:{}",bcItem.getBcCount(),bcItem.getUserId());
                    continue;
                }
                if (sbItem.getBq() || xbItem.getBq()){
                    // 综合状态，2优先级>0>1的覆盖
                    if (sbItem.getBqState() == 2 || xbItem.getBqState() == 2){
                        bqStatusInt = 2;
                        break;
                    }else if (sbItem.getBqState() == 0 || xbItem.getBqState() == 0){
                        bqStatusInt = 0;
                    }else if (bqStatusInt == -1 && (sbItem.getBqState().equals(1) || xbItem.getBqState().equals(1))){
                        bqStatusInt = 1;
                    }
                }
            }
        }
        return bqStatusInt;
    }

    /**
     * 状态,0正常，1为迟到，2为早退
     * ext- 以下字段为库里没有，但是业务层使用
     * 0 正常
     * 3 为缺勤
     * 4 为请假的记录
     * 5 为上下班有迟到早退但不算缺勤的时候
     * */
    public String convertStateToText(Integer state) {
        return switch (state) {
            case 0 -> "正常";
            case 1 -> "迟到";
            case 2 -> "早退";
            case 3 -> "缺勤";
            case 4 -> "请假";
            case 5 -> "迟到/早退";
            default -> "未知";
        };
    }


}
