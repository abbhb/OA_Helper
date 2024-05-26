package com.qc.printers.common.signin.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.qc.printers.common.common.CustomException;
import com.qc.printers.common.common.MyString;
import com.qc.printers.common.signin.dao.*;
import com.qc.printers.common.signin.domain.dto.SigninGroupDateUserDto;
import com.qc.printers.common.signin.domain.dto.SigninLogCliBcDto;
import com.qc.printers.common.signin.domain.dto.SigninLogRealYiQianDaoDto;
import com.qc.printers.common.signin.domain.entity.*;
import com.qc.printers.common.signin.domain.resp.SigninGroupDateRealResp;
import com.qc.printers.common.signin.domain.resp.SigninGroupDateResp;
import com.qc.printers.common.signin.mapper.SigninGroupRuleMapper;
import com.qc.printers.common.signin.service.SigninDeviceMangerService;
import com.qc.printers.common.signin.service.SigninLogService;
import com.qc.printers.common.user.dao.UserDao;
import com.qc.printers.common.user.domain.entity.SysDept;
import com.qc.printers.common.user.domain.entity.User;
import com.qc.printers.common.user.service.ISysDeptService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
    private SigninDeviceMangerService signinDeviceMangerService;


    /**
     * signinImage为base64
     *
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
        if (signinLog.getSigninWay().equals("face")) {
            if (StringUtils.isEmpty(signinLog.getSigninImage())) throw new CustomException("请上传原始签到图");

        }
        if (!signinLog.getSigninWay().equals("system")) {
            if (StringUtils.isEmpty(signinLog.getSigninDeviceId())) throw new CustomException("必须提供设备id");
        }
        signinLogDao.save(signinLog);
        this.addSigninLogCliByLog(signinLog);
        return "记录成功";
    }

    @Transactional
    @Override
    public List<SigninLogCli> getUserInDateAllLogCli(Long userId, LocalDate date) {
        // 考勤组更新改为强制次日生效 然后规则粒度改为天，这样不会出现同一天，不同时段签到的人结果不同
        return null;
    }

    @Override
    public boolean getUserAskForLeave(Long userId, LocalDate date, Long bcId, Integer bcCount) {

        // 暂未实现
        return false;
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
        signinGroupRuleLambdaQueryWrapper.ge(SigninGroupRule::getStartTime,date);
        signinGroupRuleLambdaQueryWrapper.and(QueryWrapper->QueryWrapper.le(SigninGroupRule::getEndTime,date).or().isNull(SigninGroupRule::getEndTime));
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
         /**支持的模式，Set为该模式下的设备，不需要对记录进行校验，添加记录的时候已经校验过了
        List<SigninWay> signinWays = rulesInfo.getSigninWays();
        Map<String,Set<String>> typeDeviceIds = new HashMap<>();
        for (SigninWay signinWay : signinWays) {
            if (!typeDeviceIds.containsKey(signinWay.getType())){
                Set<String> objects = new HashSet<>();
                objects.add(signinWay.getDeviceId());
                typeDeviceIds.put(signinWay.getType(),objects);
                continue;
            }
            Set<String> strings = typeDeviceIds.get(signinWay.getType());
            strings.add(signinWay.getDeviceId());
            typeDeviceIds.put(signinWay.getType(),strings);
        }*/
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

            // 把用户一天的记录全查出来，在业务层去循环，而不一条一查
            LambdaQueryWrapper<SigninLogCli> signinLogCliLambdaQueryWrapper = new LambdaQueryWrapper<>();
            signinLogCliLambdaQueryWrapper.eq(SigninLogCli::getLogDatetime,date);
            signinLogCliLambdaQueryWrapper.eq(SigninLogCli::getUserId,user.getId());
            List<SigninLogCli> list = signinLogCliDao.list(signinLogCliLambdaQueryWrapper);
            if (list==null){
                list = new ArrayList<>();
            }
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
            List<SigninLogCliBcDto> logListT = new ArrayList<>();
            for (int i = 1; i <= signinBc.getEveryDay(); i++) {
                // 几个班次就到几

                //[fix:也不一定，说不定有傻逼请假了也来打卡，还迟到早退] 直接一开始就排除请假的
                boolean userAskForLeave = this.getUserAskForLeave(userId, date, signinBc.getId(), i);
                if (userAskForLeave){
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
                // 上下班分别key为0或1
                // 首先是上下班是否都存在
                if (signinLogCliMap.containsKey(0)&&signinLogCliMap.containsKey(1)){
                    SigninLogCliBcDto signinLogCli = new SigninLogCliBcDto();
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
                        signinLogCli.setUserId(user.getId());
                        signinLogCli.setBcCount(i);
                        signinLogCli.setLogDatetime(date);
                        signinLogCli.setSblogTime(shangbanQingKuang.getLogTime());
                        signinLogCli.setXblogTime(xiabanQingKuang.getLogTime());
                        logListT.add(signinLogCli);
                        continue;
                    }
                    // 下面就不是正常，肯定是迟到或者早退的情况
                    signinLogCli.setState(5);
                    signinLogCli.setUserId(user.getId());
                    signinLogCli.setBcCount(i);
                    signinLogCli.setLogDatetime(date);
                    signinLogCli.setSblogTime(shangbanQingKuang.getLogTime());
                    signinLogCli.setXblogTime(xiabanQingKuang.getLogTime());
                    if (!shangbanQingKuang.getState().equals(0)){
                        signinLogCli.setSbchidao(shangbanQingKuang.getStateTime());
                    }
                    if (!xiabanQingKuang.getState().equals(0)){
                        signinLogCli.setXbzaotui(xiabanQingKuang.getStateTime());
                    }
                    logListT.add(signinLogCli);
                    continue;
                }
                // 接下来的情况就肯定是只存在上班或者只存在下班了，不存在请假就是缺勤，因为变更也是在都存在的情况下迟到早退的变更！
                SigninLogCliBcDto signinLogCli = new SigninLogCliBcDto();
                signinLogCli.setState(3);
                signinLogCli.setUserId(user.getId());
                signinLogCli.setBcCount(i);
                logListT.add(signinLogCli);
                continue;
            }
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

    /**
     * 实时大屏结果
     * @param groupId
     * @return
     */
    @Override
    public SigninGroupDateRealResp exportSigninGroupRealTime(String groupId) {
        SigninGroupDateRealResp signinGroupDateRealResp = new SigninGroupDateRealResp();
        LocalDate now = LocalDate.now();
        LambdaQueryWrapper<SigninGroupRule> signinGroupRuleLambdaQueryWrapper = new LambdaQueryWrapper<>();
        signinGroupRuleLambdaQueryWrapper.eq(SigninGroupRule::getGroupId,groupId);
        signinGroupRuleLambdaQueryWrapper.ge(SigninGroupRule::getStartTime,now);
        signinGroupRuleLambdaQueryWrapper.and(QueryWrapper->QueryWrapper.le(SigninGroupRule::getEndTime,now).or().isNull(SigninGroupRule::getEndTime));
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
//        if (!xqToId.containsKey(String.valueOf(now.getDayOfWeek().getValue()))){
//            // 今日无需考勤
//            signinGroupDateRealResp.setKaoqingString("今日无考勤任务!");
//        }
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
            signinGroupDateRealResp.setKaoqingString("当前处于xx上班阶段");//或者当前不在打卡时段
            // 需要把昨日的数据填上
            return signinGroupDateRealResp;
        }
        if ((xqToId.containsKey(zuorixingqiji))&&(xqToId.containsKey(String.valueOf(now.getDayOfWeek().getValue())))){
            // 昨日有考勤任务&&今日也有
            signinGroupDateRealResp.setKaoqingString("今日无考勤任务!");
            // 今日考勤数据

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
            return signinGroupDateRealResp;
        }


        return null;
    }


    @Transactional
    public void addSigninLogCliByLog(SigninLog signinLog) {
        SigninLogCli signinLogCli = new SigninLogCli();
        signinLogCli.setUserId(signinLog.getUserId());
        signinLogCli.setFromLog(signinLog.getId());
        // 创建一个 DateTimeFormatter 对象，用于指定时间格式
        DateTimeFormatter formatterasd = DateTimeFormatter.ofPattern("HH:mm:ss");

        // 使用 formatter 对象将 LocalDateTime 格式化为指定格式的字符串
        String formattedDateTimeasd = signinLog.getSigninTime().format(formatterasd);
        signinLogCli.setLogTime(formattedDateTimeasd);
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
            LocalDateTime timeOnlyCurrent = signinTime.withYear(2000).withMonth(1).withDayOfMonth(1);
            LocalDateTime timeOnlyStart = modifiedDateTime.withYear(2000).withMonth(1).withDayOfMonth(1);
            LocalDateTime timeOnlySB = dateTime.withYear(2000).withMonth(1).withDayOfMonth(1);
            LocalDateTime timeOnlyEnd = modifiedDateTimeEnd.withYear(2000).withMonth(1).withDayOfMonth(1);
            // 比较当前时间与起始时间和结束时间的关系
            int resultStart = timeOnlyCurrent.compareTo(timeOnlyStart);
            int resultEnd = timeOnlyCurrent.compareTo(timeOnlyEnd);
            //上班
            if (resultStart >= 0 && resultEnd <= 0) {
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
                SigninLogCli signinLogCliDaoOne = signinLogCliDao.getOne(signinLogCliLambdaQueryWrapper);
                if (signinLogCliDaoOne != null) {
                    // 暂时只记录上班前最早的，后续的肯定没那么早
                    continue;
                }
                signinLogCli1.setStateTime(signinLogCli1.getState().equals(1) ? Math.abs((int) ChronoUnit.MINUTES.between(timeOnlyCurrent, timeOnlySB)) : 0);

                signinLogCliDao.save(signinLogCli1);
                // 在之间，记录,同班次不可能出现上班和下班交叉，直接进入下一班此查找
                continue;
            }
            String xbTime = rules.get(i).getXbTime();
            // 将字符串解析为 LocalTime 对象
            Integer xbStartTime = rules.get(i).getXbStartTime();
            Integer xbEndTime = rules.get(i).getXbEndTime();


            String[] splitXB = xbTime.split(":");


            LocalDateTime dateTimeXB = currentDateTime.withHour(Integer.valueOf(splitXB[0])).withMinute(Integer.valueOf(splitXB[1])).withSecond(Integer.valueOf(splitXB[2]));


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
            LocalDateTime timeOnlyXBStart = modifiedDateTimeXB.withYear(2000).withMonth(1).withDayOfMonth(1);
            LocalDateTime timeOnlydateTimeXBBB = dateTimeXB.withYear(2000).withMonth(1).withDayOfMonth(1);
            LocalDateTime timeOnlyXBEnd = modifiedDateTimeXBEnd.withYear(2000).withMonth(1).withDayOfMonth(1);
            // 比较当前时间与起始时间和结束时间的关系
            int resultStartXB = timeOnlyCurrent.compareTo(timeOnlyXBStart);
            int resultEndXB = timeOnlyCurrent.compareTo(timeOnlyXBEnd);
            if (resultStartXB >= 0 && resultEndXB <= 0) {
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
                SigninLogCli signinLogCliDaoOne = signinLogCliDao.getOne(signinLogCliLambdaQueryWrapper);
                if (signinLogCliDaoOne != null) {
                    // 下班如果刚开始是早退就允许更新时间和状态
                    if (signinLogCliDaoOne.getState().equals(2)) {
                        LambdaUpdateWrapper<SigninLogCli> signinLogCliLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
                        signinLogCliLambdaUpdateWrapper.eq(SigninLogCli::getId, signinLogCliDaoOne.getId());
                        signinLogCliLambdaUpdateWrapper.set(SigninLogCli::getLogTime, signinLog.getSigninTime().getHour() + ":" + signinLog.getSigninTime().getMinute() + ":" + signinLog.getSigninTime().getSecond());
                        signinLogCliLambdaUpdateWrapper.set(SigninLogCli::getState, signinLogCli1.getState());
                        signinLogCliLambdaUpdateWrapper.set(SigninLogCli::getFromLog, signinLogCli1.getFromLog());
                        // 更新旷班时间，如果不早退就设置时间为0
                        int fenzhong = Math.abs(((int) ChronoUnit.MINUTES.between(timeOnlyCurrent, timeOnlydateTimeXBBB)));
                        signinLogCliLambdaUpdateWrapper.set(SigninLogCli::getStateTime, signinLogCli1.getState().equals(2) ? fenzhong == 0 ? 1 : fenzhong : 0);
                        signinLogCliDao.update(signinLogCliLambdaUpdateWrapper);
                    }
                    continue;
                }
                signinLogCli1.setStateTime(signinLogCli1.getState().equals(2) ? Math.abs((int) ChronoUnit.MINUTES.between(currentDateTime, timeOnlydateTimeXBBB)) : 0);
                signinLogCliDao.save(signinLogCli1);
                // 在之间，记录,同班次不可能出现上班和下班交叉，直接进入下一班此查找
                continue;
            }
        }

    }
}
