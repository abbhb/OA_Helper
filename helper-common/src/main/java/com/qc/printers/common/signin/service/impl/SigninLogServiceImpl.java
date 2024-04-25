package com.qc.printers.common.signin.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.qc.printers.common.common.CustomException;
import com.qc.printers.common.common.MyString;
import com.qc.printers.common.signin.dao.SigninBcDao;
import com.qc.printers.common.signin.dao.SigninLogCliDao;
import com.qc.printers.common.signin.dao.SigninLogDao;
import com.qc.printers.common.signin.domain.entity.*;
import com.qc.printers.common.signin.mapper.SigninGroupRuleMapper;
import com.qc.printers.common.signin.service.SigninDeviceMangerService;
import com.qc.printers.common.signin.service.SigninLogService;
import com.qc.printers.common.user.dao.UserDao;
import com.qc.printers.common.user.domain.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;

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
    private SigninGroupRuleMapper signinGroupRuleMapper;

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
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String formattedDateTime = signinLog.getSigninTime().format(formatter);
        SigninGroupRule signinGroupByUserIdWithTime = signinGroupRuleMapper.getSigninGroupByUserIdWithTime(formattedDateTime, formattedDateTime, String.valueOf(signinLog.getUserId()));
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
                signinLogCli1.setStateTime(signinLogCli1.getState().equals(1) ? (int) ChronoUnit.MINUTES.between(timeOnlyCurrent, timeOnlySB) : 0);

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
                        int fenzhong = ((int) ChronoUnit.MINUTES.between(timeOnlyCurrent, timeOnlydateTimeXBBB));
                        signinLogCliLambdaUpdateWrapper.set(SigninLogCli::getStateTime, signinLogCli1.getState().equals(2) ? fenzhong == 0 ? 1 : fenzhong : 0);
                        signinLogCliDao.update(signinLogCliLambdaUpdateWrapper);
                    }
                    continue;
                }
                signinLogCli1.setStateTime(signinLogCli1.getState().equals(2) ? (int) ChronoUnit.MINUTES.between(currentDateTime, timeOnlydateTimeXBBB) : 0);
                signinLogCliDao.save(signinLogCli1);
                // 在之间，记录,同班次不可能出现上班和下班交叉，直接进入下一班此查找
                continue;
            }
        }

    }
}
