package com.qc.printers.common.signin.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.qc.printers.common.activiti.entity.dto.workflow.StartProcessDto;
import com.qc.printers.common.activiti.service.ProcessStartService;
import com.qc.printers.common.common.CustomException;
import com.qc.printers.common.common.MyString;
import com.qc.printers.common.common.R;
import com.qc.printers.common.common.annotation.RedissonLock;
import com.qc.printers.common.common.domain.entity.PageData;
import com.qc.printers.common.common.utils.DateUtils;
import com.qc.printers.common.common.utils.ThreadLocalUtil;
import com.qc.printers.common.common.utils.oss.OssDBUtil;
import com.qc.printers.common.config.system.signin.SigninTipMessageConfig;
import com.qc.printers.common.signin.dao.*;
import com.qc.printers.common.signin.domain.dto.*;
import com.qc.printers.common.signin.domain.entity.*;
import com.qc.printers.common.signin.domain.req.IndexPageDataWithuserReq;
import com.qc.printers.common.signin.domain.resp.AddLogExtInfo;
import com.qc.printers.common.signin.domain.resp.SigninGroupDateRealResp;
import com.qc.printers.common.signin.domain.resp.SigninGroupDateResp;
import com.qc.printers.common.signin.domain.resp.SigninLogForSelfResp;
import com.qc.printers.common.signin.mapper.SigninGroupRuleMapper;
import com.qc.printers.common.signin.mapper.SigninRenewalMapper;
import com.qc.printers.common.signin.service.SigninDeviceMangerService;
import com.qc.printers.common.signin.service.SigninLogService;
import com.qc.printers.common.user.dao.UserDao;
import com.qc.printers.common.user.dao.UserExtBaseDao;
import com.qc.printers.common.user.domain.dto.UserInfo;
import com.qc.printers.common.user.domain.entity.SysDept;
import com.qc.printers.common.user.domain.entity.User;
import com.qc.printers.common.user.domain.entity.UserExtBase;
import com.qc.printers.common.user.domain.vo.response.ws.WSFriendApply;
import com.qc.printers.common.user.domain.vo.response.ws.WSSigninPush;
import com.qc.printers.common.user.service.ISysDeptService;
import com.qc.printers.common.user.service.adapter.WSAdapter;
import com.qc.printers.common.user.service.impl.PushService;
import com.qc.printers.utils.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.activiti.engine.HistoryService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricProcessInstanceQuery;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.repository.ProcessDefinitionQuery;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SigninLogServiceImpl implements SigninLogService {
    @Autowired
    private SigninLogDao signinLogDao;

    @Autowired
    private SigninBcDao signinBcDao;
    @Autowired
    private SigninLogCliDao signinLogCliDao;

    @Autowired
    private SigninLogCliErrDao signinLogCliErrDao;

    @Autowired
    private SigninGroupRuleMapper signinGroupRuleMapper;

    @Autowired
    private SigninGroupRuleDao signinGroupRuleDao;



    @Autowired
    private ISysDeptService iSysDeptService;

    @Autowired
    private UserDao userDao;

    @Autowired
    private UserExtBaseDao userExtBaseDao;
    @Autowired
    private SigninDeviceMangerService signinDeviceMangerService;


    @Autowired
    private SigninTipMessageConfig signinTipMessageConfig;
    @Autowired
    private PushService pushService;

    @Autowired
    private SigninLogAskLeaveDao signinLogAskLeaveDao;

    @Autowired
    private SigninRenewalDao signinRenewalDao;
    @Autowired
    private SigninRenewalMapper signinRenewalMapper;
    @Autowired
    private ProcessStartService processStartService;
    @Autowired
    private HistoryService historyService;
    @Autowired
    private RepositoryService repositoryService;

    /**
     * signinImage为base64
     * 签到逻辑入库基础方法，必须接入此方法
     * @param request
     * @param signinLog
     * @return
     */
    @Transactional
    @Override
    public String addSigninlogByDevice(HttpServletRequest request, SigninLog signinLog) {
        final String signinSecret = request.getHeader(MyString.SIGNIN_DEVICE_HEADER_KEY);
        if (!signinDeviceMangerService.checkDevice(signinLog.getSigninDeviceId(), signinSecret))
            throw new CustomException("鉴权失败");
        LambdaQueryWrapper<User> userLambdaQueryWrapper = new LambdaQueryWrapper<>();
        userLambdaQueryWrapper.eq(User::getStudentId, signinLog.getStudentId());
        User one = userDao.getOne(userLambdaQueryWrapper);
        if (one == null) {
            throw new CustomException("该学号暂未绑定用户!");
        }
        signinLog.setUserId(one.getId());
        // todo:策略模式优化
        if (signinLog.getSigninWay().equals("face")) {
            if (StringUtils.isEmpty(signinLog.getSigninImage())) throw new CustomException("请上传原始签到图");
        }
        if (!signinLog.getSigninWay().equals("system")) {
            if (StringUtils.isEmpty(signinLog.getSigninDeviceId())) throw new CustomException("必须提供设备id");
        }

        boolean save = signinLogDao.save(signinLog);
        addSigninLogCliByLog(signinLog);
        return "记录成功";
    }

    @Transactional
    @Override
    public List<SigninLogCli> getUserInDateAllLogCli(Long userId, LocalDate date) {
        // 考勤组更新改为强制次日生效 然后规则粒度改为天，这样不会出现同一天，不同时段签到的人结果不同
        return null;
    }

    @Override
    public boolean getUserAskForLeave(Long userId, LocalDateTime time) {
        LambdaQueryWrapper<SigninLogAskLeave> signinLogAskLeaveLambdaQueryWrapper = new LambdaQueryWrapper<>();
        signinLogAskLeaveLambdaQueryWrapper.eq(SigninLogAskLeave::getUserId,userId);
//        此处学到新东西了，两个条件要同时生效得and，默认是or
        signinLogAskLeaveLambdaQueryWrapper
                .eq(SigninLogAskLeave::getUserId, userId)
                .and(wrapper ->
                        wrapper.le(SigninLogAskLeave::getStartTime, time)
                                .ge(SigninLogAskLeave::getEndTime, time)
                );
        int count = signinLogAskLeaveDao.count(signinLogAskLeaveLambdaQueryWrapper);
        if (count<1){
            // 这个用户在这个时间没有请假
            return false;
        }
        // 绝对有请假
        return true;
    }

    /**
     * 生成考勤组打卡情况，不是实时，包含当天所有的情况[当这天已经过了]
     * @param groupId
     * @param date
     * @return
     */
    @Override
    public SigninGroupDateResp exportSigninGgroupDate(String groupId, LocalDate date) {
        SigninGroupDateResp signinGroupDateResp = new SigninGroupDateResp();
        signinGroupDateResp.setAtendanceRequired(true);
        LambdaQueryWrapper<SigninGroupRule> signinGroupRuleLambdaQueryWrapper = new LambdaQueryWrapper<>();
        signinGroupRuleLambdaQueryWrapper.eq(SigninGroupRule::getGroupId,groupId);
        signinGroupRuleLambdaQueryWrapper.le(SigninGroupRule::getStartTime,date);
        signinGroupRuleLambdaQueryWrapper.and(QueryWrapper->QueryWrapper.gt(SigninGroupRule::getEndTime,date).or().isNull(SigninGroupRule::getEndTime));
        SigninGroupRule one = signinGroupRuleDao.getOne(signinGroupRuleLambdaQueryWrapper);
        if (one==null){
            throw new CustomException("业务异常-9005");
        }
        RulesInfo rulesInfo = one.getRulesInfo();
        List<KQSJRule> kqsj = rulesInfo.getKqsj();
        HashMap<String,Long> xqToId = new HashMap<>();
        for (KQSJRule kqsjRule : kqsj) {
            for (String s : kqsjRule.getXq().split(",")) {
                xqToId.put(s,kqsjRule.getBcId());
            }
        }
        if (!xqToId.containsKey(String.valueOf(date.getDayOfWeek().getValue()))){
            // 今日无需考勤
            signinGroupDateResp.setAtendanceRequired(false);
            return signinGroupDateResp;
        }
        // 今天这个考勤组的的班次id
        Long bcId = xqToId.get(String.valueOf(date.getDayOfWeek().getValue()));
        SigninBc signinBc = signinBcDao.getById(bcId);
        if (signinBc==null){
            throw new CustomException("班次不存在");
        }
        List<BcRule> bcRules1 = JSON.parseArray(signinBc.getRules().toString(), BcRule.class);
        log.info("err{}", bcRules1);

        bcRules1.sort(new Comparator<BcRule>() {
            @Override
            public int compare(BcRule p1, BcRule p2) {
                return p1.getCount() - p2.getCount();
            }
        });
        signinBc.setRules(bcRules1);

        // 忘了干啥的，反正总是0目前
        Integer signinType = rulesInfo.getSigninType();
        // 该考勤组的用户，需要统计
        List<Long> userIds = rulesInfo.getUserIds();
        List<SigninGroupDateUserDto> userLogList = new ArrayList<>();
        List<SigninGroupDateUserDto> userErrorLogList = new ArrayList<>();

        for (Long userId : userIds) {
            // 逐个用户去扫描
            User user = userDao.getById(userId);
            if (user==null){
                // 用户都没了，肯定已经不存在了
                continue;
            }
            SysDept sysDept = iSysDeptService.getById(user.getDeptId());
            String deptNameAll = sysDept.getDeptNameAll();
            SigninGroupDateUserDto signinGroupDateUserDto = new SigninGroupDateUserDto();
            signinGroupDateUserDto.setDeptName(deptNameAll);
            signinGroupDateUserDto.setName(user.getName());
            signinGroupDateUserDto.setDeptId(sysDept.getId());
            signinGroupDateUserDto.setBcCount(signinBc.getEveryDay());
            // todo:我感觉此处可以优化成把一天的记录全查出来，在业务层放入map，key为用户id
            // 把用户一天的记录全查出来，在业务层去循环，而不一条一查
            LambdaQueryWrapper<SigninLogCli> signinLogCliLambdaQueryWrapper = new LambdaQueryWrapper<>();
            signinLogCliLambdaQueryWrapper.eq(SigninLogCli::getLogDatetime,date);
            signinLogCliLambdaQueryWrapper.eq(SigninLogCli::getUserId,user.getId());

            List<SigninLogCli> list = signinLogCliDao.list(signinLogCliLambdaQueryWrapper);
            if (list==null){
                list = new ArrayList<>();
            }
            List<SigninLogCliBcDto> logListT = getUserCliListBySigninDataWithBC(user.getId(), date, list, signinBc);

            signinGroupDateUserDto.setLogList(logListT);

            boolean zhengchang = true;
            int zuizhongzhuangtai = 0;
            for (SigninLogCliBcDto signinLogCliBcDto : logListT) {
                // 0,4 << 5 <<3
                if (!signinLogCliBcDto.getState().equals(0)&&!signinLogCliBcDto.getState().equals(4)){
                    zhengchang = false;
                }
                if (signinLogCliBcDto.getState().equals(4)){
                    if (zuizhongzhuangtai==0){
                        zuizhongzhuangtai = 4;
                    }
                }
                if (signinLogCliBcDto.getState().equals(5)){
                    if (zuizhongzhuangtai==0||zuizhongzhuangtai==4){
                        zuizhongzhuangtai = 5;
                    }
                }
                if (signinLogCliBcDto.getState().equals(3)){
                    if (zuizhongzhuangtai==0||zuizhongzhuangtai==4||zuizhongzhuangtai==5){
                        zuizhongzhuangtai = 3;
                    }
                }
            }
            signinGroupDateUserDto.setState(zuizhongzhuangtai);
            if (!zhengchang){

                userErrorLogList.add(signinGroupDateUserDto);
            }
            userLogList.add(signinGroupDateUserDto);

        }
        signinGroupDateResp.setUserLogList(userLogList);
        signinGroupDateResp.setUserErrorLogList(userErrorLogList);
        signinGroupDateResp.setNumberOfPeopleSupposedToCome(userLogList.size());
        signinGroupDateResp.setNumberOfError(userErrorLogList.size());
        signinGroupDateResp.setNumberOfFullAttendance(signinGroupDateResp.getNumberOfPeopleSupposedToCome()-signinGroupDateResp.getNumberOfError());
        return signinGroupDateResp;
    }

    private SigninLogCliErr getRfewgwe23123error(Long chuliId) {
        LambdaQueryWrapper<SigninLogCliErr> signinLogCliErrLambdaQueryWrapper = new LambdaQueryWrapper<>();
        signinLogCliErrLambdaQueryWrapper
                .eq(SigninLogCliErr::getSigninLogCliId, chuliId)
                .orderByDesc(SigninLogCliErr::getUpdateTime)
                // 限制结果为1条，即最新的一条
                .last("LIMIT 1");
        return signinLogCliErrDao.getOne(signinLogCliErrLambdaQueryWrapper);
    }

    /**
     * 实时大屏结果
     * @param groupId
     * @return
     */
    @Override
    public SigninGroupDateRealResp exportSigninGroupRealTime(String groupId) {
        SigninGroupDateRealResp signinGroupDateRealResp = new SigninGroupDateRealResp();
        LocalDate now = LocalDate.now();
        LocalDateTime nowDateTime = LocalDateTime.now();
        log.info("currentTime:{}",nowDateTime);
        LambdaQueryWrapper<SigninGroupRule> signinGroupRuleLambdaQueryWrapper = new LambdaQueryWrapper<>();
        signinGroupRuleLambdaQueryWrapper.eq(SigninGroupRule::getGroupId,groupId);
        signinGroupRuleLambdaQueryWrapper.le(SigninGroupRule::getStartTime,now);
        signinGroupRuleLambdaQueryWrapper.and(QueryWrapper->QueryWrapper.gt(SigninGroupRule::getEndTime,now).or().isNull(SigninGroupRule::getEndTime));
        SigninGroupRule one = signinGroupRuleDao.getOne(signinGroupRuleLambdaQueryWrapper);
        if (one==null){
            throw new CustomException("业务异常-9005");
        }

        RulesInfo rulesInfo = one.getRulesInfo();
        List<Long> kqUserIds = rulesInfo.getUserIds();
        List<KQSJRule> kqsj = rulesInfo.getKqsj();
        HashMap<String,Long> xqToId = new HashMap<>();
        for (KQSJRule kqsjRule : kqsj) {
            for (String s : kqsjRule.getXq().split(",")) {
                xqToId.put(s,kqsjRule.getBcId());
            }
        }
        // 昨日考勤信息直接调方法拿,但是昨日会不会也无考勤
        String zuorixingqiji = String.valueOf(now.getDayOfWeek().getValue());
        if (!zuorixingqiji.equals("1")){
            zuorixingqiji =String.valueOf(now.getDayOfWeek().getValue()-1);
        }else {
            zuorixingqiji = "7";
        }
        if ((!xqToId.containsKey(zuorixingqiji))&&(!xqToId.containsKey(String.valueOf(now.getDayOfWeek().getValue())))){
            // 昨日也无需考勤
            signinGroupDateRealResp.setKaoqingString("今日无考勤任务!");

            signinGroupDateRealResp.setNumberOfChiDao(0);
            signinGroupDateRealResp.setNumberOfActualArrival(0);
            signinGroupDateRealResp.setNumberOfLeave(0);
            signinGroupDateRealResp.setNumberOfZaoTUi(0);
            signinGroupDateRealResp.setWeiQianDao(new ArrayList<>());
            signinGroupDateRealResp.setYiQianDao(new ArrayList<>());
            signinGroupDateRealResp.setNumberOfPeopleSupposedToCome(0);
            signinGroupDateRealResp.setZuoRiChuQingLv("100");
            signinGroupDateRealResp.setZuoRiQueQing(new ArrayList<>());
            return signinGroupDateRealResp;
        }
        // 今天这个考勤组的的班次id
        Long bcId = xqToId.get(String.valueOf(now.getDayOfWeek().getValue()));
        if ((xqToId.containsKey(zuorixingqiji))&&(!xqToId.containsKey(String.valueOf(now.getDayOfWeek().getValue())))){
            // 昨日有考勤任务&&今日无
            signinGroupDateRealResp.setKaoqingString("今日无考勤任务!");
            signinGroupDateRealResp.setNumberOfZaoTUi(0);
            signinGroupDateRealResp.setWeiQianDao(new ArrayList<>());
            signinGroupDateRealResp.setYiQianDao(new ArrayList<>());
            signinGroupDateRealResp.setNumberOfActualArrival(0);
            signinGroupDateRealResp.setNumberOfLeave(0);
            signinGroupDateRealResp.setNumberOfPeopleSupposedToCome(0);
            signinGroupDateRealResp.setNumberOfChiDao(0);
            signinGroupDateRealResp.setNumberOfZaoTUi(0);
            // 昨日相关的就这两
            // 需要把昨日的数据填上👇
            boolean zuoriCunZai = true;

            try {
                SigninGroupDateResp zuoriData = this.exportSigninGgroupDate(groupId, now.minusDays(1L));
                if (!zuoriData.getAtendanceRequired()){
                    zuoriCunZai = false;
                }
                Integer numberOfPeopleSupposedToCome = zuoriData.getNumberOfPeopleSupposedToCome();
                Integer numberOfError = zuoriData.getNumberOfError();
                if (numberOfError.equals(0)){// 昨日无任何异常
                    signinGroupDateRealResp.setZuoRiChuQingLv("100");
                    // 昨日全部直接出勤
                    signinGroupDateRealResp.setZuoRiQueQing(new ArrayList<>());
                }else {
                    List<SigninLogRealYiQianDaoDto> signinLogRealYiQianDaoDtos = new ArrayList<>();
                    // 昨日有异常，但不一定是缺勤，可能是迟到早退，排除掉这种情况
                    List<SigninGroupDateUserDto> userErrorLogList = zuoriData.getUserErrorLogList();
                    for (SigninGroupDateUserDto signinGroupDateUserDto : userErrorLogList) {
                        if (signinGroupDateUserDto.getState().equals(3)){
                            SigninLogRealYiQianDaoDto signinLogRealYiQianDaoDto = new SigninLogRealYiQianDaoDto();
                            signinLogRealYiQianDaoDto.setTag("缺勤");
                            signinLogRealYiQianDaoDto.setName(signinGroupDateUserDto.getName());
                            signinLogRealYiQianDaoDto.setDeptName(signinGroupDateUserDto.getDeptName());
                            signinLogRealYiQianDaoDtos.add(signinLogRealYiQianDaoDto);
                        }
                    }
                    signinGroupDateRealResp.setZuoRiQueQing(signinLogRealYiQianDaoDtos);
                    signinGroupDateRealResp.setZuoRiChuQingLv(String.valueOf(100 * (((numberOfPeopleSupposedToCome - signinLogRealYiQianDaoDtos.size()) * 1.0) / numberOfPeopleSupposedToCome)));
                }
            }catch (Exception e){
                // 昨日不存在
                zuoriCunZai = false;
            }
            if (!zuoriCunZai){
                signinGroupDateRealResp.setZuoRiQueQing(new ArrayList<>());
                signinGroupDateRealResp.setZuoRiChuQingLv("100");
            }
            // 到此都是昨日数据填充

            // 需要把昨日的数据填上
            return signinGroupDateRealResp;
        }
        if ((!xqToId.containsKey(zuorixingqiji))&&(xqToId.containsKey(String.valueOf(now.getDayOfWeek().getValue())))){
            // 昨日无考勤任务&&今日有
            signinGroupDateRealResp.setZuoRiChuQingLv("100");
            signinGroupDateRealResp.setZuoRiQueQing(new ArrayList<>());
            SigninBc signinBc = signinBcDao.getById(bcId);
            if (signinBc==null){
                throw new CustomException("班次不存在:90002");
            }
            //往下是今日的考勤数据计算,此对象里的班次规则返回是对应好的，也就是如果是找不到就是没有，不在打卡但是已经经过某此打卡也会返回这次

            List<BcRule> bcRules = JSON.parseArray(JSON.toJSONString(signinBc.getRules()), BcRule.class);
            signinBc.setRules(bcRules);
            SigninBcTimeRuleDto bcTimeRule = this.getBcTimeRule(nowDateTime,bcRules);
            if (bcTimeRule.getState().equals(2)){
                // 当前不在打卡时段，且找不到任何班次已经过去了的，直接返回全部没到就行，不用查表了
                returnProcessingBeforeWorkResults(signinGroupDateRealResp,kqUserIds);
                return signinGroupDateRealResp;
            }
            if (bcTimeRule.getState().equals(1)){
                // 不在打卡时间段，但是找得到最近的上下班（当前时间不在某日的第一个打卡班次之前）
                // 找到最近的数据
                // 这里本来就以某次上班或下班的数据作为是否到了的标准，所以下面的迟到和早退其实不会同时出现
                signinGroupDateRealResp.setKaoqingString("当前不在打卡时段");
                extracted(signinGroupDateRealResp, now, kqUserIds, signinBc, bcTimeRule);
                return signinGroupDateRealResp;

            }
            if (bcTimeRule.getState().equals(0)){
                // 在打卡时段
                if (bcTimeRule.getSxBState().equals(0)){
                    // 当前在上班时间段，实时统计该时段的
                    signinGroupDateRealResp.setKaoqingString("上班打卡中["+bcTimeRule.getStartTime()+"-"+bcTimeRule.getEndTime()+"]");

                    extracted(signinGroupDateRealResp, now, kqUserIds, signinBc, bcTimeRule);

                }
                if (bcTimeRule.getSxBState().equals(1)){
                    // 当前在下班时间段，实时统计该时段的
                    signinGroupDateRealResp.setKaoqingString("下班打卡中["+bcTimeRule.getStartTime()+"-"+bcTimeRule.getEndTime()+"]");
                    extracted(signinGroupDateRealResp, now, kqUserIds, signinBc, bcTimeRule);
                }

                return signinGroupDateRealResp;
            }

            return signinGroupDateRealResp;
        }
        if ((xqToId.containsKey(zuorixingqiji))&&(xqToId.containsKey(String.valueOf(now.getDayOfWeek().getValue())))){
            // 昨日有考勤任务&&今日也有
            SigninBc signinBc = signinBcDao.getById(bcId);
            if (signinBc==null){
                throw new CustomException("班次不存在:90002");
            }
            // 今日考勤数据
            List<BcRule> bcRules = JSON.parseArray(JSON.toJSONString(signinBc.getRules()), BcRule.class);
            log.info("考勤统计bug排除-bcRules{},原始{}",bcRules,signinBc.getRules());
            signinBc.setRules(bcRules);
            SigninBcTimeRuleDto bcTimeRule = this.getBcTimeRule(nowDateTime, bcRules);
            log.info("考勤统计bug排除-nowDateTimee具体{},{}",nowDateTime,bcRules);

            if (bcTimeRule.getState().equals(2)){
                // 当前不在打卡时段，且找不到任何班次已经过去了的，直接返回全部没到就行，不用查表了
                returnProcessingBeforeWorkResults(signinGroupDateRealResp, kqUserIds);
            }
            if (bcTimeRule.getState().equals(1)){
                // 不在打卡时间段，但是找得到最近的上下班（当前时间不在某日的第一个打卡班次之前）
                // 找到最近的数据
                // 这里本来就以某次上班或下班的数据作为是否到了的标准，所以下面的迟到和早退其实不会同时出现
                signinGroupDateRealResp.setKaoqingString("当前不在打卡时段");
                extracted(signinGroupDateRealResp, now, kqUserIds, signinBc, bcTimeRule);
            }
            if (bcTimeRule.getState().equals(0)){
                // 在打卡时段
                if (bcTimeRule.getSxBState().equals(0)){
                    // 当前在上班时间段，实时统计该时段的
                    signinGroupDateRealResp.setKaoqingString("上班打卡中["+bcTimeRule.getStartTime()+"-"+bcTimeRule.getEndTime()+"]");

                    extracted(signinGroupDateRealResp,now,kqUserIds,signinBc,bcTimeRule);


                }
                if (bcTimeRule.getSxBState().equals(1)){
                    // 当前在下班时间段，实时统计该时段的
                    signinGroupDateRealResp.setKaoqingString("下班打卡中["+bcTimeRule.getStartTime()+"-"+bcTimeRule.getEndTime()+"]");
                    extracted(signinGroupDateRealResp,now,kqUserIds,signinBc,bcTimeRule);

                }

            }
            // 如果

            // 需要把昨日的数据填上👇
            boolean zuoriCunZai = true;

            try {
                SigninGroupDateResp zuoriData = this.exportSigninGgroupDate(groupId, now.minusDays(1L));
                if (!zuoriData.getAtendanceRequired()){
                    zuoriCunZai = false;
                }
                Integer numberOfPeopleSupposedToCome = zuoriData.getNumberOfPeopleSupposedToCome();
                Integer numberOfError = zuoriData.getNumberOfError();
                if (numberOfError.equals(0)){// 昨日无任何异常
                    signinGroupDateRealResp.setZuoRiChuQingLv("100");
                    // 昨日全部直接出勤
                    signinGroupDateRealResp.setZuoRiQueQing(new ArrayList<>());
                }else {
                    List<SigninLogRealYiQianDaoDto> signinLogRealYiQianDaoDtos = new ArrayList<>();
                    // 昨日有异常，但不一定是缺勤，可能是迟到早退，排除掉这种情况
                    List<SigninGroupDateUserDto> userErrorLogList = zuoriData.getUserErrorLogList();
                    for (SigninGroupDateUserDto signinGroupDateUserDto : userErrorLogList) {
                        if (signinGroupDateUserDto.getState().equals(3)){
                            SigninLogRealYiQianDaoDto signinLogRealYiQianDaoDto = new SigninLogRealYiQianDaoDto();
                            signinLogRealYiQianDaoDto.setTag("缺勤");
                            signinLogRealYiQianDaoDto.setName(signinGroupDateUserDto.getName());
                            signinLogRealYiQianDaoDto.setDeptName(signinGroupDateUserDto.getDeptName());
                            signinLogRealYiQianDaoDtos.add(signinLogRealYiQianDaoDto);
                        }
                    }
                    signinGroupDateRealResp.setZuoRiQueQing(signinLogRealYiQianDaoDtos);
                    signinGroupDateRealResp.setZuoRiChuQingLv(String.valueOf(100 * (((numberOfPeopleSupposedToCome - signinLogRealYiQianDaoDtos.size()) * 1.0) / numberOfPeopleSupposedToCome)));
                }
            }catch (Exception e){
                // 昨日不存在
                zuoriCunZai = false;
            }
            if (!zuoriCunZai){
                signinGroupDateRealResp.setZuoRiQueQing(new ArrayList<>());
                signinGroupDateRealResp.setZuoRiChuQingLv("100");
            }
            // 到此都是昨日数据填充
        }

        return signinGroupDateRealResp;
    }

    /**
     * 抽出来set传入的返回对象signinGroupDateRealResp
     * 计算有考勤任务但是还没到第一个班次之前的返回
     * @param signinGroupDateRealResp
     * @param kqUserIds
     */
    private void returnProcessingBeforeWorkResults(SigninGroupDateRealResp signinGroupDateRealResp, List<Long> kqUserIds) {
        signinGroupDateRealResp.setKaoqingString("今日还未上班!");
        signinGroupDateRealResp.setNumberOfChiDao(0);// 还没开始打卡，迟什么到
        signinGroupDateRealResp.setNumberOfLeave(0);
        Integer numberOfLeave = 0;
        signinGroupDateRealResp.setNumberOfZaoTUi(0);
        signinGroupDateRealResp.setNumberOfActualArrival(0);
        List<SigninLogRealYiQianDaoDto> signinLogRealYiQianDaoDtos = new ArrayList<>();
        for (Long kqUserId : kqUserIds) {
            SigninLogRealYiQianDaoDto signinLogRealYiQianDaoDto = new SigninLogRealYiQianDaoDto();
            User byId = userDao.getById(kqUserId);
            if (byId==null){
                continue;// 人都不存在了
            }

            signinLogRealYiQianDaoDto.setName(byId.getName());
            signinLogRealYiQianDaoDto.setTag("缺勤");
            SysDept deptServiceById = iSysDeptService.getById(byId.getDeptId());
            if (deptServiceById==null){
                signinLogRealYiQianDaoDto.setDeptName("部门不存在");
            }else {
                signinLogRealYiQianDaoDto.setDeptName(deptServiceById.getDeptNameAll());
            }
            // 请假的，还没到任何一个班次，不考虑请假展示，没上班谈请假不对
            // 以每个班次的上班时间和下班时间来看，有一个在请假就算该班次请假
            // 使用DateTimeFormatter解析时间字符串
//                    DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
//                    log.info("考勤统计bug排除-bcrule具体{}",bcTimeRule);
//                    LocalTime sbtime = LocalTime.parse(bcTimeRule.getBcRule().getSbTime(), timeFormatter);
//                    LocalTime xbtime = LocalTime.parse(bcTimeRule.getBcRule().getXbTime(), timeFormatter);
//                    // 将LocalDate和LocalTime组合成LocalDateTime
//                    LocalDateTime sb_dateTime = now.atTime(sbtime);
//                    LocalDateTime xb_dateTime = now.atTime(xbtime);
//                    boolean userAskForLeave_s = this.getUserAskForLeave(kqUserId, sb_dateTime);
//                    boolean userAskForLeave_x = this.getUserAskForLeave(kqUserId, xb_dateTime);
//                    log.info("tag:请假-表示sb{}表示下班{}",userAskForLeave_s,userAskForLeave_x);
//                    if (userAskForLeave_s||userAskForLeave_x){
//                        // 这个班次用户已经请假了，直接不往后查
//                        signinLogRealYiQianDaoDto.setTag("请假");
//                        numberOfLeave+=1;
//                    }
            signinLogRealYiQianDaoDtos.add(signinLogRealYiQianDaoDto);
        }
        signinGroupDateRealResp.setWeiQianDao(signinLogRealYiQianDaoDtos);// 所有人
        signinGroupDateRealResp.setYiQianDao(new ArrayList<>());// 空
        signinGroupDateRealResp.setNumberOfLeave(0);// 请假的真实人数，无班次不谈请假
        signinGroupDateRealResp.setNumberOfPeopleSupposedToCome(signinLogRealYiQianDaoDtos.size());//应到
    }

    @Transactional
    @Override
    public AddLogExtInfo addSigninlogByDevicePlus(HttpServletRequest request, SigninLog signinLog) {
        String s = addSigninlogByDevice(request, signinLog);
        AddLogExtInfo addLogExtInfo = new AddLogExtInfo();
        try {
            LambdaQueryWrapper<User> userLambdaQueryWrapper = new LambdaQueryWrapper<>();
            userLambdaQueryWrapper.eq(User::getStudentId, signinLog.getStudentId());
            User one = userDao.getOne(userLambdaQueryWrapper);
            addLogExtInfo.setAvatarUrl(OssDBUtil.toUseUrl(one.getAvatar()));
            SysDept sysDept = iSysDeptService.getById(one.getDeptId());
            addLogExtInfo.setDeptName(sysDept.getDeptNameAll());
            UserExtBase userExtBase = userExtBaseDao.getById(one.getId());
            if (userExtBase!=null&&StringUtils.isNotEmpty(userExtBase.getIdPhoto())){
                addLogExtInfo.setAvatarUrl(OssDBUtil.toUseUrl(userExtBase.getIdPhoto()));
            }
            WSSigninPush wsSigninPush = new WSSigninPush(addLogExtInfo.getAvatarUrl(), addLogExtInfo.getDeptName(), one.getStudentId(), one.getName());
            if (userExtBase!=null&&StringUtils.isNotEmpty(userExtBase.getZsxm())){
                wsSigninPush.setName(userExtBase.getZsxm());
            }
            signinPushToLed(wsSigninPush,one.getId());
        }catch (Exception e){
            log.error(e.getMessage());
        }
        return addLogExtInfo;
    }

    @RedissonLock(key = "#userId")
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void replacementVisaApprovalByService(Long userId, LocalDateTime time, String actId,String reason) {
        if (userId==null){
            throw new CustomException("请提供补签的对象");
        }
        if (time==null){
            throw new CustomException("补签必须提供时间");
        }
        if (time.isAfter(LocalDateTime.now())){
            throw new CustomException("补签不能补签当前时间之后！无法完成你的请求");
        }
        User byId = userDao.getById(userId);
        if (byId==null){
            throw new CustomException("对象不存在");
        }
        if (StringUtils.isEmpty(byId.getStudentId())){
            throw new CustomException("学号不存在");
        }

        SigninLog signinLog = new SigninLog();
        signinLog.setSigninImage(null);
        signinLog.setSigninWay("renewal");
        signinLog.setRemark("补签，act单号"+actId);
        signinLog.setSigninTime(time);
        signinLog.setStudentId(byId.getStudentId());
        signinLogDao.save(signinLog);

        addSigninLogCliByLog(signinLog);

        SigninRenewal signinRenewal = SigninRenewal.builder()
                .renewalTime(time)
                .createTime(LocalDateTime.now())
                .renewalReason(reason)
                .signinLogId(signinLog.getId())
                .renewalAboutActId(actId).userId(userId).build();
        signinRenewalDao.save(signinRenewal);

    }

    /**
     * 发起补签流程
     * @param signinRenewals
     * @return
     */
    @Transactional
    @Override
    public String logRenewalSignin(List<SigninRenewal> signinRenewals) {
        UserInfo currentUser = ThreadLocalUtil.getCurrentUser();
        String userId = String.valueOf(currentUser.getId());
        if (signinRenewals==null){
            throw new CustomException("单据内容不存在");
        }
        for (SigninRenewal signinRenewal : signinRenewals) {
            if (signinRenewal.getRenewalTime()==null){
                throw new CustomException("存在为空的补签时间");
            }
            if (StringUtils.isEmpty(signinRenewal.getRenewalReason())){
                throw new CustomException("必须包含补签理由!");
            }
        }
        ProcessDefinitionQuery query = repositoryService.createProcessDefinitionQuery();
        query.processDefinitionKey("Process_system_2");
        query.latestVersion();
        ProcessDefinition processDefinition = query.singleResult();
        if (processDefinition==null){
            throw new CustomException("审批流程被挂起或不存在，请确保此key存在");
        }

        if (processDefinition.isSuspended()){
            throw new CustomException("审批流程被挂起");
        }
        String deploymentId = processDefinition.getId();

        // 首先判断当前有没有进行中的审批，还没结束
        HistoricProcessInstanceQuery query2 = historyService.createHistoricProcessInstanceQuery()
                .startedBy(userId)
                .notDeleted();
        // 根据流程key查询 注意是等于不是模糊查询
        List<HistoricProcessInstance> processSystemList = query2.processDefinitionId(deploymentId).unfinished().list();
        HistoricProcessInstance processSystem1 = null;

        if (processSystemList!=null&&processSystemList.size()!=0){
            processSystem1 = processSystemList.get(0);
        }
        if (processSystem1!=null){
            throw new CustomException("当前已经在有进行中的单据在审批了，可以尝试取消或者联系管理完成审批再尝试~");
        }
        if (org.apache.commons.lang.StringUtils.isEmpty(deploymentId)){
            throw new CustomException("系统异常");
        }
        StartProcessDto startProcessDto = new StartProcessDto();
        startProcessDto.setDefinitionId(deploymentId);
        Map<String,Object> map = new HashMap<>();
        // 往map里存入对象
        map.put("bq_signin_list_json", JsonUtils.toStr(signinRenewals));
        startProcessDto.setVariables(map);

        processStartService.startProcess(startProcessDto,userId);
        return "补签申请成功";

    }

    @Override
    public SigninLogForSelfResp getUserDaySelf(Long userId, LocalDate date) {
        SigninLogForSelfResp signinLogForSelfResp = new SigninLogForSelfResp();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter formatter_sfm = DateTimeFormatter.ofPattern("HH:mm:ss");
        String formattedDateTime = date.format(formatter);
        // 设置公共变量 👇
        signinLogForSelfResp.setCurrentDate(formattedDateTime);
        signinLogForSelfResp.setNeedSB(true);
        signinLogForSelfResp.setState(0);
        signinLogForSelfResp.setBcCount(0);
        signinLogForSelfResp.setUserId(userId);
        signinLogForSelfResp.setErrMsg("");

        signinLogForSelfResp.setBcDetail(new ArrayList<>());

        // 设置公共变量 👆
        SigninGroupRule signinGroupByUserIdWithTime = signinGroupRuleMapper.getSigninGroupByUserIdWithTime(formattedDateTime, formattedDateTime, String.valueOf(userId));
        if (signinGroupByUserIdWithTime==null){
            // 找不到任何考勤组
            return SigninLogForSelfResp.builder()
                    .bcCount(0)
                    .state(0)
                    .userId(userId)
                    .currentXQ(DateUtils.dateToWeek(formattedDateTime)
                    )
                    .currentDate(formattedDateTime)
                    .needSB(false)
                    .build();
        }
        // 需要考勤的人 走规则匹配
        signinLogForSelfResp.setCurrentXQ(DateUtils.dateToWeek(formattedDateTime));
        RulesInfo rulesInfo = signinGroupByUserIdWithTime.getRulesInfo();
        List<KQSJRule> kqsj = rulesInfo.getKqsj();
        HashMap<String,Long> xqToId = new HashMap<>();
        for (KQSJRule kqsjRule : kqsj) {
            for (String s : kqsjRule.getXq().split(",")) {
                xqToId.put(s,kqsjRule.getBcId());
            }
        }
        if (!xqToId.containsKey(String.valueOf(date.getDayOfWeek().getValue()))){
            // 今日无需考勤
            signinLogForSelfResp.setNeedSB(false);
            return signinLogForSelfResp;
        }
        // 今天这个考勤组的的班次id
        Long bcId = xqToId.get(String.valueOf(date.getDayOfWeek().getValue()));
        SigninBc signinBc = signinBcDao.getById(bcId);
        if (signinBc==null){
            throw new CustomException("班次不存在-请联系管理员检查班次映射");
        }
        List<BcRule> bcRules1 = JSON.parseArray(signinBc.getRules().toString(), BcRule.class);
        log.info("err{}", bcRules1);
        bcRules1.sort(new Comparator<BcRule>() {
            @Override
            public int compare(BcRule p1, BcRule p2) {
                return p1.getCount() - p2.getCount();
            }
        });
        signinBc.setRules(bcRules1);
        signinLogForSelfResp.setBcCount(signinBc.getEveryDay());
        LambdaQueryWrapper<SigninLogCli> signinLogCliLambdaQueryWrapper = new LambdaQueryWrapper<>();
        signinLogCliLambdaQueryWrapper.eq(SigninLogCli::getLogDatetime,date);
        signinLogCliLambdaQueryWrapper.eq(SigninLogCli::getUserId,userId);

        List<SigninLogCli> list = signinLogCliDao.list(signinLogCliLambdaQueryWrapper);
        if (list==null){
            list = new ArrayList<>();
        }
        List<SigninLogCliBcDto> userCliListBySigninDataWithBC = getUserCliListBySigninDataWithBC(userId, date, list, signinBc);
        // 已经拿到了每日每人班次处理聚合处理后的结果了

        SigninLog firstLogDayByUserId = signinLogDao.getFirstLogDayByUserId(userId, date);
        SigninLog lastLogDayByUserId = signinLogDao.getLastLogDayByUserId(userId, date);
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        if (firstLogDayByUserId!=null){
            signinLogForSelfResp.setFirstTime(firstLogDayByUserId.getSigninTime().format(dateTimeFormatter));
        }
        if (lastLogDayByUserId!=null){
            signinLogForSelfResp.setEndTime(lastLogDayByUserId.getSigninTime().format(dateTimeFormatter));

        }

        Map<Integer, Map<Integer, SigninLogCli>> signinLogCliBcCountMap = getSigninLogCliBcCountMap(list);
        for (SigninLogCliBcDto signinLogCliBcDto : userCliListBySigninDataWithBC) {
            Integer bcCount = signinLogCliBcDto.getBcCount();
            // 异常原因补充
            if (signinLogCliBcDto.getState().equals(3)){
                if (signinLogCliBcCountMap.containsKey(bcCount)){
                    Map<Integer, SigninLogCli> integerSigninLogCliMap = signinLogCliBcCountMap.get(bcCount);
                    if (!integerSigninLogCliMap.containsKey(0)){
                        // 上班不存在打卡
                        BcRule bcRule = signinBc.getRules().get(bcCount - 1);
                        LocalTime sbTimeObject = LocalTime.parse(bcRule.getSbTime());
                        String sb_start = sbTimeObject.minusMinutes(bcRule.getSbStartTime()).format(DateTimeFormatter.ofPattern("HH:mm:ss"));
                        String sb_end = sbTimeObject.plusMinutes(bcRule.getSbEndTime()).format(DateTimeFormatter.ofPattern("HH:mm:ss"));
                        signinLogForSelfResp.setErrMsg(
                                signinLogForSelfResp.getErrMsg()
                                        + sb_start +" - " +
                                        sb_end +
                                        " [上班]宽限期内未打卡；"
                        );

                    }
                    if (!integerSigninLogCliMap.containsKey(1)){
                        // 下班不存在打卡
                        BcRule bcRule = signinBc.getRules().get(bcCount - 1);
                        LocalTime xbTimeObject = LocalTime.parse(bcRule.getXbTime());
                        String xb_start = xbTimeObject.minusMinutes(bcRule.getXbStartTime()).format(DateTimeFormatter.ofPattern("HH:mm:ss"));
                        String xb_end = xbTimeObject.plusMinutes(bcRule.getXbEndTime()).format(DateTimeFormatter.ofPattern("HH:mm:ss"));
                        signinLogForSelfResp.setErrMsg(
                                signinLogForSelfResp.getErrMsg()
                                        + xb_start +" - " +
                                        xb_end +
                                        " [下班]宽限期内未打卡；"
                        );
                    }
                }else{
                    // 上班不存在打卡
                    BcRule bcRule = signinBc.getRules().get(bcCount - 1);
                    LocalTime sbTimeObject = LocalTime.parse(bcRule.getSbTime());
                    String sb_start = sbTimeObject.minusMinutes(bcRule.getSbStartTime()).format(DateTimeFormatter.ofPattern("HH:mm:ss"));
                    String sb_end = sbTimeObject.plusMinutes(bcRule.getSbEndTime()).format(DateTimeFormatter.ofPattern("HH:mm:ss"));
                    signinLogForSelfResp.setErrMsg(
                            signinLogForSelfResp.getErrMsg()
                                    + sb_start +" - " +
                                    sb_end +
                                    " [上班]宽限期内未打卡；"
                    );
                    // 上下班都没打卡

                    // 下班不存在打卡
                    LocalTime xbTimeObject = LocalTime.parse(bcRule.getXbTime());
                    String xb_start = xbTimeObject.minusMinutes(bcRule.getXbStartTime()).format(DateTimeFormatter.ofPattern("HH:mm:ss"));
                    String xb_end = xbTimeObject.plusMinutes(bcRule.getXbEndTime()).format(DateTimeFormatter.ofPattern("HH:mm:ss"));
                    signinLogForSelfResp.setErrMsg(
                            signinLogForSelfResp.getErrMsg()
                                    + xb_start +" - " +
                                    xb_end +
                                    " [下班]宽限期内未打卡；"
                    );
//                    signinLogForSelfResp.setErrMsg(signinLogForSelfResp.getErrMsg()+"时间段[上班或下班]未打卡");
                }
            }
            if (signinLogCliBcDto.getState().equals(1)||signinLogCliBcDto.getState().equals(2)||signinLogCliBcDto.getState().equals(5)){
                // 迟到 早退 没缺勤说明肯定同时存在上班或下班
                Map<Integer, SigninLogCli> integerSigninLogCliMap = signinLogCliBcCountMap.get(bcCount);
                if (!integerSigninLogCliMap.containsKey(0)||!integerSigninLogCliMap.containsKey(1))throw new CustomException("正常情况不包含--3600500");
                SigninLogCli signinSBLogCli = integerSigninLogCliMap.get(0);
                SigninLogCli signinXBLogCli = integerSigninLogCliMap.get(1);
                if (signinSBLogCli.getState().equals(1)){
                    // 存在上班迟到
                    // 打卡晚于迟到宽限时间1分钟；应打卡09:00，实打卡09:03，迟到3分钟
                    signinLogForSelfResp.setErrMsg(
                            signinLogForSelfResp.getErrMsg()
                                    + "打卡晚于迟到宽限时间；应打卡"
                                    + signinBc.getRules().get(bcCount - 1).getSbTime()
                                    + "，实打卡"
                                    + signinSBLogCli.getLogTime()
                                    + " ，迟到"+ signinSBLogCli.getStateTime() +"分钟；"
                    );
                }
                if (signinXBLogCli.getState().equals(2)){
                    // 存在下班早退
                    signinLogForSelfResp.setErrMsg(
                            signinLogForSelfResp.getErrMsg()
                                    + "打卡早于早退宽限时间；应打卡"
                                    + signinBc.getRules().get(bcCount - 1).getXbTime()
                                    + "，实打卡"
                                    + signinXBLogCli.getLogTime()
                                    + " ，早退"+ signinXBLogCli.getStateTime() +"分钟；"
                    );
                }
            }
            SigninLogCliBcDto signinLogCliBcDto1 = new SigninLogCliBcDto(userId,date,bcCount);
            signinLogCliBcDto1.setState(signinLogCliBcDto.getState());// 外层状态保持一致，因为每个班次状态已经有了
            signinLogForSelfResp.getBcDetail().add(signinLogCliBcDto1);
            // 具体log补充
            if (signinLogCliBcCountMap.containsKey(bcCount)){
                Map<Integer, SigninLogCli> integerSigninLogCliMap = signinLogCliBcCountMap.get(bcCount);
                // 上班先入为主
                signinLogCliBcDto1.getSbItem().setState(3);
                signinLogCliBcDto1.getSbItem().setBq(false);
                signinLogCliBcDto1.getSbItem().setTimeY(LocalDateTime.of(date,LocalTime.parse(bcRules1.get(bcCount-1).getSbTime())));
                if (integerSigninLogCliMap.containsKey(0)){
                    // 上班存在打卡
                    SigninLogCli signinLogCliWithSB = integerSigninLogCliMap.get(0);
                    signinLogCliBcDto1.getSbItem().setState(signinLogCliWithSB.getState());
                    signinLogCliBcDto1.getSbItem().setTimeS(LocalDateTime.of(date,LocalTime.parse(signinLogCliWithSB.getLogTime())));
                    if (signinLogCliWithSB.getState().equals(1)||signinLogCliWithSB.getState().equals(2)){
                        // 存在缺勤时间
                        signinLogCliBcDto1.getSbItem().setQueQingTime(signinLogCliWithSB.getStateTime());
                    }
                    // 查看当前班次时间内是否存在补签
                    BcRule bcRule = signinBc.getRules().get(bcCount - 1);
                    LocalTime sbTimeObject = LocalTime.parse(bcRule.getSbTime());
                    SigninRenewal signinRenewal = signinRenewalMapper.hasExistRenewal(LocalDateTime.of(date, sbTimeObject
                            .minusMinutes(bcRule.getSbStartTime())),
                            LocalDateTime.of(date,
                                    sbTimeObject
                                    .plusMinutes(bcRule.getSbEndTime())),
                            userId);
                    if (signinRenewal!=null){
                        signinLogCliBcDto1.getSbItem().setBq(true);
                        signinLogCliBcDto1.getSbItem().setBqState(signinRenewal.getState());
                        signinLogCliBcDto1.getSbItem().setBqTime(signinRenewal.getRenewalTime());// 不管是否成功，先返回补签点
                    }
                }
                // 下班先入为主
                signinLogCliBcDto1.getXbItem().setBq(false);
                signinLogCliBcDto1.getXbItem().setState(3);
                signinLogCliBcDto1.getXbItem().setTimeY(LocalDateTime.of(date,LocalTime.parse(bcRules1.get(bcCount-1).getXbTime())));
                if (integerSigninLogCliMap.containsKey(1)){
                    // 下班存在打卡
                    // 下班
                    SigninLogCli signinLogCliWithXB = integerSigninLogCliMap.get(1);
                    signinLogCliBcDto1.getXbItem().setState(signinLogCliWithXB.getState());
                    signinLogCliBcDto1.getXbItem().setTimeS(LocalDateTime.of(date,LocalTime.parse(signinLogCliWithXB.getLogTime())));
                    if (signinLogCliWithXB.getState().equals(1)||signinLogCliWithXB.getState().equals(2)){
                        // 存在缺勤时间
                        signinLogCliBcDto1.getXbItem().setQueQingTime(signinLogCliWithXB.getStateTime());
                    }
                    // 查看当前班次时间内是否存在补签
                    BcRule bcRule = signinBc.getRules().get(bcCount - 1);
                    LocalTime xbTimeObject = LocalTime.parse(bcRule.getXbTime());
                    SigninRenewal signinRenewal = signinRenewalMapper.hasExistRenewal(LocalDateTime.of(date, xbTimeObject
                                    .minusMinutes(bcRule.getXbStartTime())),
                            LocalDateTime.of(date,
                                    xbTimeObject
                                            .plusMinutes(bcRule.getXbEndTime())),
                            userId);
                    if (signinRenewal!=null){
                        signinLogCliBcDto1.getXbItem().setBq(true);
                        signinLogCliBcDto1.getXbItem().setBqState(signinRenewal.getState());
                        signinLogCliBcDto1.getXbItem().setBqTime(signinRenewal.getRenewalTime());// 不管是否成功，先返回补签点
                    }

                }
            }else{
                // 上下班都没打卡
                signinLogCliBcDto1.getSbItem().setState(3);
                signinLogCliBcDto1.getSbItem().setBq(false);
                signinLogCliBcDto1.getSbItem().setTimeY(LocalDateTime.of(date,LocalTime.parse(bcRules1.get(bcCount-1).getSbTime())));
                // 下班
                signinLogCliBcDto1.getXbItem().setBq(false);
                signinLogCliBcDto1.getXbItem().setState(3);
                signinLogCliBcDto1.getXbItem().setTimeY(LocalDateTime.of(date,LocalTime.parse(bcRules1.get(bcCount-1).getXbTime())));
            }

            Integer zuizhongzhuangtao = 0;// 默认正常
            Integer stateS = signinLogCliBcDto1.getSbItem().getState();// 上班状态
            Integer stateX = signinLogCliBcDto1.getXbItem().getState();// 下班状态
            // 上班迟到1，下班早退2最终状态5，迟到早退
            if (((stateS.equals(1)||stateS.equals(2)||stateS.equals(5))&&(stateX.equals(1)||stateX.equals(2)||stateX.equals(5)))){
                zuizhongzhuangtao = 5;
            }
            if (stateS.equals(3)||stateX.equals(3)){
                zuizhongzhuangtao = 3;
            }
            // 暂时有一班请假算请假
            if (stateS.equals(4)||stateX.equals(4)){
                zuizhongzhuangtao = 4;
            }
            signinLogCliBcDto1.setState(zuizhongzhuangtao);
        }


        // log聚合状态填充
        Integer zuizhongzhuangtai = 0;
        for (SigninLogCliBcDto signinLogCliBcDto : signinLogForSelfResp.getBcDetail()) {
            // 每个具体班次
            // 0,4 << 5 <<3
//            if (!signinLogCliBcDto.getState().equals(0)&&!signinLogCliBcDto.getState().equals(4)){
//                zhengchang = false;
//            }
            if (signinLogCliBcDto.getState().equals(4)){
                if (zuizhongzhuangtai==0){
                    zuizhongzhuangtai = 4;
                }
            }
            if (signinLogCliBcDto.getState().equals(5)){
                if (zuizhongzhuangtai==0||zuizhongzhuangtai==4){
                    zuizhongzhuangtai = 5;
                }
            }
            if (signinLogCliBcDto.getState().equals(3)){
                if (zuizhongzhuangtai==0||zuizhongzhuangtai==4||zuizhongzhuangtai==5){
                    zuizhongzhuangtai = 3;
                }
            }
        }
        signinLogForSelfResp.setState(zuizhongzhuangtai);

        return signinLogForSelfResp;
    }


    private PageData<LocalDate> getPagedData(Integer pageNum, Integer pageSize, LocalDate startDate, LocalDate endDate) {
        // 获取当前日期
        LocalDate today = LocalDate.now();
        PageData<LocalDate> pageData= new PageData<>();
        pageData.setCurrent(Long.valueOf(pageNum));
        pageData.setSize(Long.valueOf(pageSize));
        if (startDate==null){
            throw new CustomException("请提供起始日期");
        }

        if (endDate == null || endDate.isAfter(today)) {
            endDate = today;
        }

        // 获取符合日期范围的日期列表
        List<LocalDate> allDates = new ArrayList<>();
        LocalDate date = endDate;
        while (!date.isBefore(startDate)) {
            allDates.add(date);
            date = date.minusDays(1);
        }
        pageData.setTotal(Long.valueOf(allDates.size()));
        pageData.setPages((long) Math.ceil((pageData.getTotal() * 1.0) / pageData.getSize()));
        if (allDates.isEmpty()){
            pageData.setRecords(Collections.emptyList());
            return pageData;
        }

        // 计算分页
        int total = allDates.size();
        int fromIndex = (pageNum - 1) * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, total);

        if (fromIndex >= total) {
            pageData.setRecords(Collections.emptyList());

            return pageData; // 没有更多数据
        }
        pageData.setRecords(allDates.subList(fromIndex, toIndex));

        return pageData;
    }

    /**
     * 获取最老的用户签到的日期
     * @param userId
     * @return
     */
    private LocalDate oldUserDate(Long userId){


        LambdaQueryWrapper<SigninLog> signinLogLambdaQueryWrapper = new LambdaQueryWrapper<>();
        signinLogLambdaQueryWrapper
                .select(SigninLog::getSigninTime)
                .orderByAsc(SigninLog::getSigninTime)
                .last("LIMIT 1");

        LocalDate userLastDateTime = signinGroupRuleMapper.getUserLastDateTime(String.valueOf(userId));
        if (userLastDateTime!=null){
            return userLastDateTime;
        }
        throw new CustomException("无需查询");
    }
    @Override
    public PageData<SigninLogForSelfResp> indexPageDataWithuser(IndexPageDataWithuserReq indexPageDataWithuserReq) {
        UserInfo currentUser = ThreadLocalUtil.getCurrentUser();
        Long userId = currentUser.getId();
        LocalDate startEnd = oldUserDate(userId);
        if (indexPageDataWithuserReq.getStart()!=null){
            if (!indexPageDataWithuserReq.getStart().isBefore(startEnd)){
                startEnd = indexPageDataWithuserReq.getStart();
            }
        }
        PageData<LocalDate> pagedData = getPagedData(indexPageDataWithuserReq.getPageNum(),
                indexPageDataWithuserReq.getPageSize(),
                startEnd,
                indexPageDataWithuserReq.getEnd() != null ?
                        indexPageDataWithuserReq.getEnd() : LocalDate.now());

        PageData<SigninLogForSelfResp> signinLogServicePageData = new PageData<>();
        signinLogServicePageData.setRecords(new ArrayList<>());
        signinLogServicePageData.setTotal(pagedData.getTotal());
        signinLogServicePageData.setCurrent(pagedData.getCurrent());
        signinLogServicePageData.setPages(pagedData.getPages());
        signinLogServicePageData.setSize(pagedData.getSize());
        for (LocalDate localDate : pagedData.getRecords()) {
            SigninLogForSelfResp userDaySelf = getUserDaySelf(userId, localDate);
            signinLogServicePageData.getRecords().add(userDaySelf);
        }
        return signinLogServicePageData;
    }


    /**
     * 通过原始cli数据列表和班次得到处理后的对象列表-方便后续修改统计算法
     * @param userId
     * @param date 某一天
     * @param list 源数据
     * @param signinBc
     * @return
     */
    private List<SigninLogCliBcDto> getUserCliListBySigninDataWithBC(Long userId, LocalDate date, List<SigninLogCli> list, SigninBc signinBc) {
        Map<Integer, Map<Integer, SigninLogCli>> signinLogCliBcCountMap = getSigninLogCliBcCountMap(list);

        List<SigninLogCliBcDto> logListT = new ArrayList<>();
        for (int i = 1; i <= signinBc.getEveryDay(); i++) {
            // 几个班次就到几

            //[fix:也不一定，说不定有傻逼请假了也来打卡，还迟到早退] 直接一开始就排除请假的
            // 以每个班次的上班时间和下班时间来看，有一个在请假就算该班次请假

            BcRule bcRule = signinBc.getRules().get(i - 1);

            // 使用DateTimeFormatter解析时间字符串
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
            LocalTime sbtime = LocalTime.parse(bcRule.getSbTime(), timeFormatter);
            LocalTime xbtime = LocalTime.parse(bcRule.getXbTime(), timeFormatter);
            // 将LocalDate和LocalTime组合成LocalDateTime
            LocalDateTime sb_dateTime = date.atTime(sbtime);
            LocalDateTime xb_dateTime = date.atTime(xbtime);


            boolean userAskForLeave_s = this.getUserAskForLeave(userId, sb_dateTime);
            boolean userAskForLeave_x = this.getUserAskForLeave(userId, xb_dateTime);
//            log.info("tag:请假-表示sb{}表示下班{}",userAskForLeave_s,userAskForLeave_x);

            if (userAskForLeave_s||userAskForLeave_x){
                SigninLogCliBcDto signinLogCli = new SigninLogCliBcDto();
                // 有请假记录,直接计为请假
                signinLogCli.setState(4);
                signinLogCli.setBcCount(i);
                signinLogCli.setUserId(userId);
                logListT.add(signinLogCli);
                continue;
            }
            // 连班次都不存在，不说上下班了，肯定就是没打卡
            if (!signinLogCliBcCountMap.containsKey(i)){
                SigninLogCliBcDto signinLogCli = new SigninLogCliBcDto();
                // 请假情况一开始直接排除
                signinLogCli.setState(3);
                signinLogCli.setBcCount(i);
                signinLogCli.setUserId(userId);

                logListT.add(signinLogCli);
                continue;
            }
            // 如果班次存在，也就是打过卡，看看上下班是不是都没异常，如果有上下班缺一个直接找请假，都不缺直接对比状态

            Map<Integer, SigninLogCli> signinLogCliMap = signinLogCliBcCountMap.get(i);
            // add: 上下班都存在结合处理表，如果有修改的状态机
            if (signinLogCliMap.containsKey(0)){
                // shang班状态是否被纠正
                SigninLogCli signinLogCli12312 = signinLogCliMap.get(0);

                SigninLogCliErr rfewgwe23123error = getRfewgwe23123error(signinLogCli12312.getId());
                if (rfewgwe23123error!=null){
                    signinLogCli12312.setState(rfewgwe23123error.getNewState());
                    signinLogCliMap.put(0,signinLogCli12312);
                }
            }
            if (signinLogCliMap.containsKey(1)){
                // 下班状态是否被纠正
                SigninLogCli signinLogCli2141241241 = signinLogCliMap.get(1);
                SigninLogCliErr rfewgwe23123error = getRfewgwe23123error(signinLogCli2141241241.getId());
                if (rfewgwe23123error!=null){
                    signinLogCli2141241241.setState(rfewgwe23123error.getNewState());
                    signinLogCliMap.put(1,signinLogCli2141241241);
                }
            }
            // 上下班分别key为0或1
            // 首先是上下班是否都存在
            if (signinLogCliMap.containsKey(0)&&signinLogCliMap.containsKey(1)){
                SigninLogCliBcDto signinLogCli = new SigninLogCliBcDto();
                signinLogCli.setXbItem(new SigninLogCliBcItem());
                SigninLogCli shangbanQingKuang = signinLogCliMap.get(0);
                SigninLogCli xiabanQingKuang = signinLogCliMap.get(1);
                // 在往下之前，需要查看变更表有没有手动标注迟到早退为正常的，有的话那就以变更表为主
                LambdaQueryWrapper<SigninLogCliErr> signinLogCliErrLambdaQueryWrapper = new LambdaQueryWrapper<>();
                signinLogCliErrLambdaQueryWrapper.eq(SigninLogCliErr::getSigninLogCliId,shangbanQingKuang.getId());
                signinLogCliErrLambdaQueryWrapper.orderByDesc(SigninLogCliErr::getUpdateTime);
                // 上班变更
                List<SigninLogCliErr> sbBG = signinLogCliErrDao.list(signinLogCliErrLambdaQueryWrapper);
                if (sbBG.size()!=0){
                    shangbanQingKuang.setState(sbBG.get(0).getNewState());
                }
                // 下班变更👇
                LambdaQueryWrapper<SigninLogCliErr> signinLogCliErrLambdaQueryWrapper2 = new LambdaQueryWrapper<>();
                signinLogCliErrLambdaQueryWrapper2.eq(SigninLogCliErr::getSigninLogCliId,xiabanQingKuang.getId());
                signinLogCliErrLambdaQueryWrapper2.orderByDesc(SigninLogCliErr::getUpdateTime);
                List<SigninLogCliErr> xbBG = signinLogCliErrDao.list(signinLogCliErrLambdaQueryWrapper2);
                if (sbBG.size()!=0){
                    shangbanQingKuang.setState(sbBG.get(0).getNewState());
                }
                if (xbBG.size()!=0){
                    xiabanQingKuang.setState(xbBG.get(0).getNewState());
                }
                // 状态更新完毕👆
                // 只会存在0，1，2，要么正常，要么迟到早退，只有都正常才正常，否则就让返回的State为5
                if (shangbanQingKuang.getState().equals(0)&&xiabanQingKuang.getState().equals(0)){
                    // 这种没得说，就是正常
                    signinLogCli.setState(0);
                    signinLogCli.setUserId(userId);
                    signinLogCli.setBcCount(i);
                    signinLogCli.setLogDatetime(date);

                    signinLogCli.getSbItem().setTimeS(LocalDateTime.of(date, LocalTime.parse(shangbanQingKuang.getLogTime())));
                    signinLogCli.getXbItem().setTimeS(LocalDateTime.of(date,LocalTime.parse(xiabanQingKuang.getLogTime())));

                    logListT.add(signinLogCli);
                    continue;
                }
                // 下面就不是正常，肯定是迟到或者早退的情况
                signinLogCli.setState(5);
                signinLogCli.setUserId(userId);
                signinLogCli.setBcCount(i);
                signinLogCli.setLogDatetime(date);

                signinLogCli.getSbItem().setTimeS(LocalDateTime.of(date,LocalTime.parse(shangbanQingKuang.getLogTime())));
                signinLogCli.getXbItem().setTimeS(LocalDateTime.of(date,LocalTime.parse(xiabanQingKuang.getLogTime())));

                // 迟到或早退时间
                signinLogCli
                        .getSbItem()
                        .setQueQingTime(
                                !shangbanQingKuang.getState().equals(0)
                                        ?shangbanQingKuang.getStateTime() :
                                        xiabanQingKuang.getStateTime()
                        );

                logListT.add(signinLogCli);
                continue;
            }
            // 接下来的情况就肯定是只存在上班或者只存在下班了，不存在请假就是缺勤，因为变更也是在都存在的情况下迟到早退的变更！
            SigninLogCliBcDto signinLogCli = new SigninLogCliBcDto();
            signinLogCli.setState(3);
            signinLogCli.setUserId(userId);
            signinLogCli.setBcCount(i);
            logListT.add(signinLogCli);
            continue;
        }
        return logListT;
    }

    @NotNull
    private static Map<Integer, Map<Integer, SigninLogCli>> getSigninLogCliBcCountMap(List<SigninLogCli> list) {
        // bc_count作为map的key
        // Map<bc_count,<上下班,SigninLogCli>>
        Map<Integer,Map<Integer,SigninLogCli>> signinLogCliBcCountMap = new HashMap<>();

        for (SigninLogCli signinLogCli : list) {
            if (!signinLogCliBcCountMap.containsKey(signinLogCli.getBcCount())){
                Map<Integer,SigninLogCli> signinLogCliMap = new HashMap<>();
                signinLogCliMap.put(signinLogCli.getStartEnd(),signinLogCli);
                signinLogCliBcCountMap.put(signinLogCli.getBcCount(),signinLogCliMap);
                continue;
            }
            Map<Integer, SigninLogCli> signinLogCliMap = signinLogCliBcCountMap.get(signinLogCli.getBcCount());
            signinLogCliMap.put(signinLogCli.getStartEnd(),signinLogCli);
            signinLogCliBcCountMap.put(signinLogCli.getBcCount(),signinLogCliMap);
        }
        return signinLogCliBcCountMap;
    }

    private void signinPushToLed(WSSigninPush wsSigninPush,Long targetId) {
        if (!signinTipMessageConfig.isEnable())return;// 一旦使用也会有初始化的过程，无需再判断其他
        pushService.sendPushMsg(WSAdapter.buildSigninPushSend(wsSigninPush),Long.valueOf(signinTipMessageConfig.getUserId()));

    }

    private void extracted(SigninGroupDateRealResp signinGroupDateRealResp, LocalDate now, List<Long> kqUserIds, SigninBc signinBc, SigninBcTimeRuleDto bcTimeRule) {
        List<SigninLogRealYiQianDaoDto> yiqiandaoList = new ArrayList<>();
        List<SigninLogRealYiQianDaoDto> weiqiandaoList = new ArrayList<>();
        signinGroupDateRealResp.setYiQianDao(yiqiandaoList);// 已签到往这个里面add
        signinGroupDateRealResp.setWeiQianDao(weiqiandaoList);// 未签到往这个里面add
        Integer numberOfPeopleSupposedToCome = 0;//应到
        Integer numberOfActualArrival = 0;// 实到
        Integer numberOfLeave = 0;// 请假人数
        Integer numberOfChiDao = 0;// 迟到人数
        Integer numberOfZaoTUi = 0;// 早退人数
        if (bcTimeRule.getSxBState().equals(0)){
            // 找到的在某个班次的上班
            for (Long kqUserId : kqUserIds) {
                SigninLogRealYiQianDaoDto signinLogRealYiQianDaoDto = new SigninLogRealYiQianDaoDto();
                User byId = userDao.getById(kqUserId);
                if (byId==null){
                    continue;// 人都不存在了
                }
                numberOfPeopleSupposedToCome+=1;
                signinLogRealYiQianDaoDto.setName(byId.getName());
                SysDept deptServiceById = iSysDeptService.getById(byId.getDeptId());
                if (deptServiceById==null){
                    signinLogRealYiQianDaoDto.setDeptName("部门不存在");
                }else {
                    signinLogRealYiQianDaoDto.setDeptName(deptServiceById.getDeptNameAll());
                }
                // 先排除掉请假的
                // 以每个班次的上班时间和下班时间来看，有一个在请假就算该班次请假
                // 使用DateTimeFormatter解析时间字符串
                DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
                LocalTime sbtime = LocalTime.parse(bcTimeRule.getBcRule().getSbTime(), timeFormatter);
                LocalTime xbtime = LocalTime.parse(bcTimeRule.getBcRule().getXbTime(), timeFormatter);
                // 将LocalDate和LocalTime组合成LocalDateTime
                LocalDateTime sb_dateTime = now.atTime(sbtime);
                LocalDateTime xb_dateTime = now.atTime(xbtime);
                boolean userAskForLeave_s = this.getUserAskForLeave(kqUserId, sb_dateTime);
                boolean userAskForLeave_x = this.getUserAskForLeave(kqUserId, xb_dateTime);
//                log.info("tag:请假-表示sb{}表示下班{}",userAskForLeave_s,userAskForLeave_x);

                if (userAskForLeave_s||userAskForLeave_x){
                    // 这个班次用户已经请假了，直接不往后查
                    signinLogRealYiQianDaoDto.setTag("请假");
                    numberOfLeave+=1;
                    weiqiandaoList.add(signinLogRealYiQianDaoDto);
                    continue;
                }

                // 没请假的继续
                LambdaQueryWrapper<SigninLogCli> signinLogCliLambdaQueryWrapper = new LambdaQueryWrapper<>();
                signinLogCliLambdaQueryWrapper.eq(SigninLogCli::getLogDatetime, now);
                signinLogCliLambdaQueryWrapper.eq(SigninLogCli::getUserId,byId.getId());
                signinLogCliLambdaQueryWrapper.eq(SigninLogCli::getBcCount, bcTimeRule.getBcRule().getCount());
                signinLogCliLambdaQueryWrapper.eq(SigninLogCli::getStartEnd, bcTimeRule.getSxBState());
                List<SigninLogCli> list = signinLogCliDao.list(signinLogCliLambdaQueryWrapper);
                // 正常只存在一条，但是为了健壮性，还是用list，大于1的直接取第0条数据
                if (list.size()==0){
                    // 数据都不存在，可不缺勤
                    signinLogRealYiQianDaoDto.setTag("缺勤");
                    weiqiandaoList.add(signinLogRealYiQianDaoDto);
                    continue;
                }
                numberOfActualArrival+=1;// 只要不是请假和缺勤就是实到
                SigninLogCli signinLogCli = list.get(0);
                // 存在数据就需要结合处理表
                SigninLogCliErr rfewgwe23123error = getRfewgwe23123error(signinLogCli.getId());
                if (rfewgwe23123error!=null){
                    signinLogCli.setState(rfewgwe23123error.getNewState());
                }

                if (signinLogCli.getState().equals(0)){
                    signinLogRealYiQianDaoDto.setTag("出勤");
                    yiqiandaoList.add(signinLogRealYiQianDaoDto);
                    continue;
                }
                //这里的迟到和早退犹豫上面已经分了分支，其实之会存在其中一种，但是懒，屎山代码吧
                if(signinLogCli.getState().equals(1)){
                    signinLogRealYiQianDaoDto.setTag("迟到");
                    numberOfChiDao+=1;
                    yiqiandaoList.add(signinLogRealYiQianDaoDto);
                    continue;
                }

                if (signinLogCli.getState().equals(2)){
                    signinLogRealYiQianDaoDto.setTag("早退");
                    numberOfZaoTUi+=1;
                    yiqiandaoList.add(signinLogRealYiQianDaoDto);
                    continue;
                }

            }
        }

        if (bcTimeRule.getSxBState().equals(1)){
            // 找到的在某个班次的下班
            for (Long kqUserId : kqUserIds) {
                SigninLogRealYiQianDaoDto signinLogRealYiQianDaoDto = new SigninLogRealYiQianDaoDto();
                User byId = userDao.getById(kqUserId);
                if (byId==null){
                    continue;// 人都不存在了
                }
                numberOfPeopleSupposedToCome+=1;
                signinLogRealYiQianDaoDto.setName(byId.getName());
                SysDept deptServiceById = iSysDeptService.getById(byId.getDeptId());
                if (deptServiceById==null){
                    signinLogRealYiQianDaoDto.setDeptName("部门不存在");
                }else {
                    signinLogRealYiQianDaoDto.setDeptName(deptServiceById.getDeptNameAll());
                }
                // 先排除掉请假的
                // 以每个班次的上班时间和下班时间来看，有一个在请假就算该班次请假
                // 使用DateTimeFormatter解析时间字符串
                DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
                LocalTime sbtime = LocalTime.parse(bcTimeRule.getBcRule().getSbTime(), timeFormatter);
                LocalTime xbtime = LocalTime.parse(bcTimeRule.getBcRule().getXbTime(), timeFormatter);
                // 将LocalDate和LocalTime组合成LocalDateTime
                LocalDateTime sb_dateTime = now.atTime(sbtime);
                LocalDateTime xb_dateTime = now.atTime(xbtime);


                boolean userAskForLeave_s = this.getUserAskForLeave(kqUserId, sb_dateTime);
                boolean userAskForLeave_x = this.getUserAskForLeave(kqUserId, xb_dateTime);
                if (userAskForLeave_s||userAskForLeave_x){
                    // 这个班次用户已经请假了，直接不往后查
                    signinLogRealYiQianDaoDto.setTag("请假");
                    numberOfLeave+=1;
                    weiqiandaoList.add(signinLogRealYiQianDaoDto);
                    continue;
                }
                // 没请假的继续
                LambdaQueryWrapper<SigninLogCli> signinLogCliLambdaQueryWrapper = new LambdaQueryWrapper<>();
                signinLogCliLambdaQueryWrapper.eq(SigninLogCli::getLogDatetime, now);
                signinLogCliLambdaQueryWrapper.eq(SigninLogCli::getUserId,byId.getId());
                signinLogCliLambdaQueryWrapper.eq(SigninLogCli::getBcCount, bcTimeRule.getBcRule().getCount());
                signinLogCliLambdaQueryWrapper.eq(SigninLogCli::getStartEnd, bcTimeRule.getSxBState());
                List<SigninLogCli> list = signinLogCliDao.list(signinLogCliLambdaQueryWrapper);
                // 正常只存在一条，但是为了健壮性，还是用list，大于1的直接取第0条数据
                if (list.size()==0){
                    // 数据都不存在，可不缺勤
                    signinLogRealYiQianDaoDto.setTag("缺勤");
                    weiqiandaoList.add(signinLogRealYiQianDaoDto);

                    continue;
                }
                numberOfActualArrival+=1;// 只要不是请假和缺勤就是实到
                SigninLogCli signinLogCli = list.get(0);
                // 存在数据就需要结合处理表
                SigninLogCliErr rfewgwe23123error = getRfewgwe23123error(signinLogCli.getId());
                if (rfewgwe23123error!=null){
                    signinLogCli.setState(rfewgwe23123error.getNewState());
                }

                if (signinLogCli.getState().equals(0)){
                    signinLogRealYiQianDaoDto.setTag("出勤");
                    yiqiandaoList.add(signinLogRealYiQianDaoDto);
                    continue;
                }
                //这里的迟到和早退犹豫上面已经分了分支，其实之会存在其中一种，但是懒，屎山代码吧
                if(signinLogCli.getState().equals(1)){
                    signinLogRealYiQianDaoDto.setTag("迟到");
                    numberOfChiDao+=1;
                    yiqiandaoList.add(signinLogRealYiQianDaoDto);
                    continue;
                }

                if (signinLogCli.getState().equals(2)){
                    signinLogRealYiQianDaoDto.setTag("早退");
                    numberOfZaoTUi+=1;
                    yiqiandaoList.add(signinLogRealYiQianDaoDto);
                    continue;
                }

            }
        }
        //还有些人数设置
        signinGroupDateRealResp.setNumberOfPeopleSupposedToCome(numberOfPeopleSupposedToCome);// 应到，就是全部的人数
        signinGroupDateRealResp.setNumberOfActualArrival(numberOfActualArrival);// 实际到，确认现在在的人数，只要打了卡就算
        signinGroupDateRealResp.setNumberOfLeave(numberOfLeave);// 请假人数
        signinGroupDateRealResp.setNumberOfChiDao(numberOfChiDao);// 迟到人数
        signinGroupDateRealResp.setNumberOfZaoTUi(numberOfZaoTUi);// 早退人数，这个和迟到互斥，在此模式下，但是还是展示
    }

    /**
     * 仅适配实时接口，整体计算单独弄，这个会导致一些情况下规则为对应的班次null
     * @param currentDateTime 根据当前时间返回班次
     * @param bcRules 传入一个bc的规则列表解析
     * @return 返回提示当前处于什么班次,如果当前不在打卡时间段，那就返回最近的一次上班或者下班的结果
     * //todo:没写完 如果当前不在打卡时间段，那就返回最近的一次上班或者下班的结果
     */
    private SigninBcTimeRuleDto getBcTimeRule(LocalDateTime currentDateTime, List<BcRule> bcRules) {
        SigninBcTimeRuleDto signinBcTimeRuleDto = new SigninBcTimeRuleDto();
        LocalTime currentTime = currentDateTime.toLocalTime();
        BcRule jieguo = null;
        BcRule closestPastShift = null;
        String closestPastShiftType = null;  // 用于记录最近班次是上班还是下班
        long minTimeDifference = Long.MAX_VALUE;
        for (BcRule shift : bcRules) {
            // 一个时间段不会存在多个重复的打卡，起码在一个考勤组内，打卡时间段必须错开!
            // Check if the current time is within the start and end range for sbTime
            LocalTime sbTimeObject = LocalTime.parse(shift.getSbTime());
            LocalTime sbStartRange =  sbTimeObject.minusMinutes(shift.getSbStartTime());
            LocalTime sbEndRange = sbTimeObject.plusMinutes(shift.getSbEndTime());
            if (!currentTime.isBefore(sbStartRange) && !currentTime.isAfter(sbEndRange)) {
                signinBcTimeRuleDto.setState(0);
                signinBcTimeRuleDto.setSxBState(0);
                signinBcTimeRuleDto.setBcRule(shift);
                signinBcTimeRuleDto.setStartTime(sbStartRange.format(DateTimeFormatter.ofPattern("HH:mm:ss")));
                signinBcTimeRuleDto.setEndTime(sbEndRange.format(DateTimeFormatter.ofPattern("HH:mm:ss")));
                jieguo = shift;
                break;
            }
            LocalTime xbTimeObject = LocalTime.parse(shift.getXbTime());
            // Check if the current time is within the start and end range for xbTime
            LocalTime xbStartRange = xbTimeObject.minusMinutes(shift.getXbStartTime());
            LocalTime xbEndRange = xbTimeObject.plusMinutes(shift.getXbEndTime());
            if (!currentTime.isBefore(xbStartRange) && !currentTime.isAfter(xbEndRange)) {
                signinBcTimeRuleDto.setState(0);
                signinBcTimeRuleDto.setSxBState(1);
                signinBcTimeRuleDto.setStartTime(xbStartRange.format(DateTimeFormatter.ofPattern("HH:mm:ss")));
                signinBcTimeRuleDto.setEndTime(xbEndRange.format(DateTimeFormatter.ofPattern("HH:mm:ss")));
                signinBcTimeRuleDto.setBcRule(shift);
                jieguo = shift;
                break;

            }

            // 查找已经经过的最近的班次
            if (currentTime.isAfter(sbTimeObject)) {
                // 只记录更小的时间，取绝对值，离当前更近的
                long timeDifference = Duration.between(sbTimeObject, currentTime).toMinutes();
                if (timeDifference < minTimeDifference) {
                    minTimeDifference = timeDifference;
                    closestPastShift = shift;
                    closestPastShiftType = "sb";  // 记录是上班时间
                }
            }

            if (currentTime.isAfter(xbTimeObject)) {
                long timeDifference = Duration.between(xbTimeObject, currentTime).toMinutes();
                if (timeDifference < minTimeDifference) {
                    minTimeDifference = timeDifference;
                    closestPastShift = shift;
                    closestPastShiftType = "xb";  // 记录是下班时间
                }
            }
        }
        if (jieguo==null){
            //

            if (closestPastShift != null) {
                signinBcTimeRuleDto.setState(1); // 1表示已经经过的最近班次
                signinBcTimeRuleDto.setBcRule(closestPastShift);
                if ("sb".equals(closestPastShiftType)) {
                    signinBcTimeRuleDto.setSxBState(0); // 上班时间
                    LocalTime closestPastShiftXbTime = LocalTime.parse(closestPastShift.getXbTime());
                    LocalTime xbStartRange = closestPastShiftXbTime.minusMinutes(closestPastShift.getXbStartTime());
                    LocalTime xbEndRange = closestPastShiftXbTime.plusMinutes(closestPastShift.getXbEndTime());
                    signinBcTimeRuleDto.setStartTime(xbStartRange.format(DateTimeFormatter.ofPattern("HH:mm:ss")));
                    signinBcTimeRuleDto.setEndTime(xbEndRange.format(DateTimeFormatter.ofPattern("HH:mm:ss")));
                } else if ("xb".equals(closestPastShiftType)) {
                    signinBcTimeRuleDto.setSxBState(1); // 下班时间
                    LocalTime closestPastShiftXbTime = LocalTime.parse(closestPastShift.getSbTime());
                    LocalTime sbStartRange = closestPastShiftXbTime.minusMinutes(closestPastShift.getSbStartTime());
                    LocalTime sbEndRange = closestPastShiftXbTime.plusMinutes(closestPastShift.getSbEndTime());
                    signinBcTimeRuleDto.setStartTime(sbStartRange.format(DateTimeFormatter.ofPattern("HH:mm:ss")));
                    signinBcTimeRuleDto.setEndTime(sbEndRange.format(DateTimeFormatter.ofPattern("HH:mm:ss")));
                }
            } else {
                signinBcTimeRuleDto.setState(2); // 2表示找不到任何班次
            }
        }
        return signinBcTimeRuleDto;
    }


    @Transactional
    public void addSigninLogCliByLog(SigninLog signinLog) {
        SigninLogCli signinLogCli = new SigninLogCli();
        signinLogCli.setUserId(signinLog.getUserId());
        signinLogCli.setFromLog(signinLog.getId());
        // 创建一个 DateTimeFormatter 对象，用于指定时间格式
        DateTimeFormatter formatterasd = DateTimeFormatter.ofPattern("HH:mm:ss");

        // 使用 formatter 对象将 LocalDateTime 格式化为指定格式的字符串
        String signinFormatTime = signinLog.getSigninTime().format(formatterasd);
        signinLogCli.setLogTime(signinFormatTime);
        signinLogCli.setLogDatetime(signinLog.getSigninTime().toLocalDate());
        // 开始找与该用户匹配的规则
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String format = signinLog.getSigninTime().toLocalDate().format(formatter);
        SigninGroupRule signinGroupByUserIdWithTime = signinGroupRuleMapper.getSigninGroupByUserIdWithTime(format, format, String.valueOf(signinLog.getUserId()));
        if (signinGroupByUserIdWithTime == null) {
            return;
            // 表示无匹配的规则，这个人，这条为无效记录，只添加原始记录
        }
        boolean fangshi = false;
        for (int i = 0; i < signinGroupByUserIdWithTime.getRulesInfo().getSigninWays().size(); i++) {
            if (signinLog.getSigninWay().equals(signinGroupByUserIdWithTime.getRulesInfo().getSigninWays().get(i).getType())) {
                fangshi = true;
                break;
            }
        }
        if (!fangshi) {
            return;
            // 直接返回
        }
        List<KQSJRule> kqsj = signinGroupByUserIdWithTime.getRulesInfo().getKqsj();
        if (kqsj == null || kqsj.size() == 0) {
            return;
        }
        int isPP = -1;
        Long bcId = null;
        for (int i = 0; i < kqsj.size(); i++) {
            String xq = kqsj.get(i).getXq();
            String[] split = xq.split(",");
            for (int i1 = 0; i1 < split.length; i1++) {
                if (split[i1].equals(String.valueOf(signinLog.getSigninTime().getDayOfWeek().getValue()))) {
                    isPP = i1;
                    bcId = kqsj.get(i).getBcId();
                    break;
                }
            }
            if (isPP != -1) {
                break;
            }

        }
        if (isPP == -1 || bcId == null) {
            return;
            //没有相匹配的规则就是无考勤任务
        }
        // 找到唯一能匹配的班次了
        SigninBc signinBc = signinBcDao.getById(Long.valueOf(bcId));
        if (signinBc == null) {
            return;
            // 班次异常
        }
        // 每天几次\
        List<BcRule> rules = signinBc.getRules();
        Integer everyDay = signinBc.getEveryDay();
        List<BcRule> bcRules1 = JSON.parseArray(rules.toString(), BcRule.class);
        log.info("err{}", bcRules1);

        bcRules1.sort(new Comparator<BcRule>() {
            @Override
            public int compare(BcRule p1, BcRule p2) {
                return p1.getCount() - p2.getCount();
            }
        });
        signinBc.setRules(bcRules1);
        rules = bcRules1;
        LocalDateTime signinTime = signinLog.getSigninTime();
        for (int i = 0; i < rules.size(); i++) {
            // 距离那段时间最近或者在那段时间里，如果多个最近同时生成多条记录
            // 只记录在起始时间到结束时间内的，如果在多个起始和结束时间内的就记录多个
            String sbTime = rules.get(i).getSbTime();
//            LocalTime localTime = LocalDateTime.parse(); // 将字符串解析为 LocalTime 对象

            // 获取当前日期
            LocalDateTime currentDateTime = LocalDateTime.now();
            DayOfWeek dayOfWeek = currentDateTime.getDayOfWeek();
            // 将 LocalTime 对象与当前日期组合成 LocalDateTime 对象
//            LocalDateTime dateTime = currentDateTime.with(localTime);
            String[] split = sbTime.split(":");


            LocalDateTime dateTime = currentDateTime.withHour(Integer.valueOf(split[0])).withMinute(Integer.valueOf(split[1])).withSecond(Integer.valueOf(split[2]));
            Integer sbStartTime = rules.get(i).getSbStartTime();
            LocalDateTime modifiedDateTime = dateTime.minusSeconds(Duration.ofMinutes(sbStartTime).getSeconds());
            Integer sbEndTime = rules.get(i).getSbEndTime();
            //结束时间
            LocalDateTime modifiedDateTimeEnd = dateTime.plusSeconds(Duration.ofMinutes(sbEndTime).getSeconds());
            if (modifiedDateTimeEnd.getDayOfWeek().getValue() != modifiedDateTime.getDayOfWeek().getValue()) {
                continue;
                // 不能跨日
            }
            if (modifiedDateTimeEnd.getDayOfWeek().getValue() != dayOfWeek.getValue()) {
                continue;
                // 不能跨日
            }
            if (modifiedDateTime.getDayOfWeek().getValue() != dayOfWeek.getValue()) {
                continue;
                // 不能跨日
            }
            // 保证起始时间和截止签到时间都在一天内
            // 忽略日期部分，将日期部分设置为相同的值
            LocalDateTime timeOnlyCurrent = signinTime.withYear(2000).withMonth(1).withDayOfMonth(1).withNano(0);
            LocalDateTime timeOnlyStart = modifiedDateTime.withYear(2000).withMonth(1).withDayOfMonth(1).withNano(0);
            LocalDateTime timeOnlySB = dateTime.withYear(2000).withMonth(1).withDayOfMonth(1).withNano(0);
            LocalDateTime timeOnlyEnd = modifiedDateTimeEnd.withYear(2000).withMonth(1).withDayOfMonth(1).withNano(0);
            // 比较当前时间与起始时间和结束时间的关系
            int resultStart = timeOnlyCurrent.compareTo(timeOnlyStart);
            int resultEnd = timeOnlyCurrent.compareTo(timeOnlyEnd);
            //上班
            // fix:不允许出现两边都是闭区间
            if (resultStart >= 0 && resultEnd < 0) {
                SigninLogCli signinLogCli1 = new SigninLogCli();
                BeanUtils.copyProperties(signinLogCli, signinLogCli1);
                signinLogCli1.setStartEnd(0);
                signinLogCli1.setBcCount(rules.get(i).getCount());
                if (!timeOnlyCurrent.isBefore(timeOnlySB)) {
                    signinLogCli1.setState(1);// 上班只有0或1
                } else {
                    signinLogCli1.setState(0);// 上班只有0或1
                }

                LambdaQueryWrapper<SigninLogCli> signinLogCliLambdaQueryWrapper = new LambdaQueryWrapper<>();
                signinLogCliLambdaQueryWrapper.eq(SigninLogCli::getLogDatetime, signinLog.getSigninTime().toLocalDate());
                signinLogCliLambdaQueryWrapper.eq(SigninLogCli::getBcCount, rules.get(i).getCount());
                signinLogCliLambdaQueryWrapper.eq(SigninLogCli::getStartEnd, 0);
                // fix:修复了任意个时段只有第一个人能打卡
                signinLogCliLambdaQueryWrapper.eq(SigninLogCli::getUserId,signinLog.getUserId());
                SigninLogCli signinLogCliDaoOne = signinLogCliDao.getOne(signinLogCliLambdaQueryWrapper);
                if (signinLogCliDaoOne != null) {
                    // 暂时只记录上班前最早的，后续的肯定没那么早
                    log.info("后续上班，不是最早{}",signinLogCliDaoOne);
                    break;
                }
                signinLogCli1.setStateTime(signinLogCli1.getState().equals(1) ? Math.abs((int) ChronoUnit.MINUTES.between(timeOnlyCurrent, timeOnlySB)) : 0);
                log.info("上班记录{}",signinLogCli1);

                signinLogCliDao.save(signinLogCli1);
                // 在之间，记录,同班次不可能出现上班和下班交叉，直接进入下一班此查找
                break;
            }
            String xbTime = rules.get(i).getXbTime();
            // 将字符串解析为 LocalTime 对象
            Integer xbStartTime = rules.get(i).getXbStartTime();
            Integer xbEndTime = rules.get(i).getXbEndTime();


            String[] splitXB = xbTime.split(":");




            LocalDateTime dateTimeXB = currentDateTime.withHour(Integer.valueOf(splitXB[0])).withMinute(Integer.valueOf(splitXB[1])).withSecond(Integer.valueOf(splitXB[2])).withNano(0);


            LocalDateTime modifiedDateTimeXB = dateTimeXB.minusSeconds(Duration.ofMinutes(xbStartTime).getSeconds());
            LocalDateTime modifiedDateTimeXBEnd = dateTimeXB.plusSeconds(Duration.ofMinutes(xbEndTime).getSeconds());
            if (modifiedDateTimeXBEnd.getDayOfWeek().getValue() != modifiedDateTimeXB.getDayOfWeek().getValue()) {
                continue;
                // 不能跨日
            }
            if (modifiedDateTimeXBEnd.getDayOfWeek().getValue() != dayOfWeek.getValue()) {
                continue;
                // 不能跨日
            }
            if (modifiedDateTimeXB.getDayOfWeek().getValue() != dayOfWeek.getValue()) {
                continue;
                // 不能跨日,假日期，但是跨日肯定不对
            }
            LocalDateTime timeOnlyXBStart = modifiedDateTimeXB.withYear(2000).withMonth(1).withDayOfMonth(1).withNano(0);
            LocalDateTime timeOnlydateTimeXBBB = dateTimeXB.withYear(2000).withMonth(1).withDayOfMonth(1).withNano(0);
            LocalDateTime timeOnlyXBEnd = modifiedDateTimeXBEnd.withYear(2000).withMonth(1).withDayOfMonth(1).withNano(0);
            // 比较当前时间与起始时间和结束时间的关系
            int resultStartXB = timeOnlyCurrent.compareTo(timeOnlyXBStart);
            int resultEndXB = timeOnlyCurrent.compareTo(timeOnlyXBEnd);
            // 下班，此处考虑
            // add: 如果连班为true，则添加下一班次上班的记录，无原始记录
            // fix: 不允许出现两边都是闭区间
            if (resultStartXB >= 0 && resultEndXB < 0) {
                // 下班在时间段里
                // 下班记录逻辑，在具体跟上班或下班时间判断早退还是迟到啥的
                SigninLogCli signinLogCli1 = new SigninLogCli();
                BeanUtils.copyProperties(signinLogCli, signinLogCli1);
                signinLogCli1.setStartEnd(1);
                signinLogCli1.setBcCount(rules.get(i).getCount());
                if (timeOnlyCurrent.isBefore(timeOnlydateTimeXBBB)) {
                    signinLogCli1.setState(2);// 下班只有0或2
                } else {
                    signinLogCli1.setState(0);// 下班只有0或2
                }

                LambdaQueryWrapper<SigninLogCli> signinLogCliLambdaQueryWrapper = new LambdaQueryWrapper<>();
                signinLogCliLambdaQueryWrapper.eq(SigninLogCli::getLogDatetime, signinLog.getSigninTime().toLocalDate());
                signinLogCliLambdaQueryWrapper.eq(SigninLogCli::getBcCount, rules.get(i).getCount());
                signinLogCliLambdaQueryWrapper.eq(SigninLogCli::getStartEnd, 1);
                // fix:修复了任意个时段只有第一个人能打卡
                signinLogCliLambdaQueryWrapper.eq(SigninLogCli::getUserId,signinLog.getUserId());
                SigninLogCli signinLogCliDaoOne = signinLogCliDao.getOne(signinLogCliLambdaQueryWrapper);
                if (signinLogCliDaoOne != null) {
                    // 下班如果刚开始是早退就允许更新时间和状态
                    if (signinLogCliDaoOne.getState().equals(2)) {
                        LambdaUpdateWrapper<SigninLogCli> signinLogCliLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
                        signinLogCliLambdaUpdateWrapper.eq(SigninLogCli::getId, signinLogCliDaoOne.getId());
                        signinLogCliLambdaUpdateWrapper.set(SigninLogCli::getLogTime, signinFormatTime);
                        signinLogCliLambdaUpdateWrapper.set(SigninLogCli::getState, signinLogCli1.getState());
                        signinLogCliLambdaUpdateWrapper.set(SigninLogCli::getFromLog, signinLogCli1.getFromLog());
                        // 更新旷班时间，如果不早退就设置时间为0
                        int fenzhong = Math.abs(((int) ChronoUnit.MINUTES.between(timeOnlyCurrent, timeOnlydateTimeXBBB)));
                        signinLogCliLambdaUpdateWrapper.set(SigninLogCli::getStateTime, signinLogCli1.getState().equals(2) ? fenzhong == 0 ? 1 : fenzhong : 0);
                        signinLogCliDao.update(signinLogCliLambdaUpdateWrapper);
                        log.info("下班更新{}",fenzhong);

                        break;
                    }
                    continue;
                }
                signinLogCli1.setStateTime(signinLogCli1.getState().equals(2) ? Math.abs((int) ChronoUnit.MINUTES.between(timeOnlyCurrent, timeOnlydateTimeXBBB)) : 0);
                log.info("记录{}",signinLogCli1);
                signinLogCliDao.save(signinLogCli1);
                // 连班是否有 存在自动打卡
                if (rules.get(i)!=null && rules.get(i).getLianban()!=null && rules.get(i).getLianban()){
                    SigninLogCli signinLogCli2 = new SigninLogCli();
                    BeanUtils.copyProperties(signinLogCli, signinLogCli2);
                    if (signinLogCli1.getBcCount()+1> rules.size()){
                        log.error("ERROR","连班：{}",3600500);
                        return;
                    }
                    if (!signinLogCli1.getState().equals(0)){
                        log.info("INFO","无法连班，当前不是正常下班signinLogCli1:{}", signinLogCli1);
                        return;
                    }
                    signinLogCli2.setBcCount(signinLogCli1.getBcCount()+1);
                    signinLogCli2.setState(0);
                    signinLogCli2.setStartEnd(0);
                    signinLogCliDao.save(signinLogCli2);                }
                break;
                // 在之间，记录,同班次不可能出现上班和下班交叉，直接进入下一班此查找
            }
        }

    }


}
