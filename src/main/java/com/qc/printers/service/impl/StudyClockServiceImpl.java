package com.qc.printers.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qc.printers.common.Code;
import com.qc.printers.common.CustomException;
import com.qc.printers.common.MyString;
import com.qc.printers.common.R;
import com.qc.printers.config.StudyClockConfig;
import com.qc.printers.mapper.StudyClockMapper;
import com.qc.printers.pojo.StudyClock;
import com.qc.printers.pojo.User;
import com.qc.printers.pojo.dto.AddClock30DTO;
import com.qc.printers.pojo.dto.ClockSelfDTO;
import com.qc.printers.service.IRedisService;
import com.qc.printers.service.StudyClockService;
import com.qc.printers.utils.ThreadLocalUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class StudyClockServiceImpl extends ServiceImpl<StudyClockMapper, StudyClock> implements StudyClockService {

    private final IRedisService iRedisService;
    private final StudyClockMapper studyClockMapper;
    @Autowired
    private StudyClockConfig studyClockConfig;


    public StudyClockServiceImpl(IRedisService iRedisService, StudyClockMapper studyClockMapper) {
        this.iRedisService = iRedisService;
        this.studyClockMapper = studyClockMapper;
    }

    @Transactional
    @Override
    public void addClock30(AddClock30DTO addClock30DTO) {
        User currentUser = ThreadLocalUtil.getCurrentUser();
        if (currentUser == null) {
            throw new CustomException("登录失效", Code.DEL_TOKEN);
        }
        String value = iRedisService.getValue(MyString.study_clock + currentUser.getId());
        if (StringUtils.isNotEmpty(value)) {
            throw new CustomException("别想作弊！！！");
        }
        iRedisService.setTokenWithTime(MyString.study_clock + currentUser.getId(), "no", 28L);
        LambdaQueryWrapper<StudyClock> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(StudyClock::getUserId, currentUser.getId());
        queryWrapper.eq(StudyClock::getDate, LocalDate.now());
        StudyClock one = this.getOne(queryWrapper);
        if (one != null) {
            //累加时间即可！
            LambdaUpdateWrapper<StudyClock> studyClockLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
            studyClockLambdaUpdateWrapper.eq(StudyClock::getId, one.getId());
            studyClockLambdaUpdateWrapper.set(StudyClock::getOldTime, one.getOldTime() + 0.5);
            boolean update = this.update(studyClockLambdaUpdateWrapper);
            if (!update) {
                throw new CustomException("异常，请刷新!!!");
            }
        } else {
            StudyClock studyClock = new StudyClock();
            studyClock.setDate(LocalDate.now());
            studyClock.setOldTime(0.5);
            studyClock.setFirstTime(LocalDateTime.now());
            studyClock.setUserId(currentUser.getId());
            this.save(studyClock);
        }
    }

    @Override
    public ClockSelfDTO getClockSelf() {
        User currentUser = ThreadLocalUtil.getCurrentUser();
        if (currentUser == null) {
            throw new CustomException("登录失效", Code.DEL_TOKEN);
        }
        LambdaQueryWrapper<StudyClock> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(StudyClock::getUserId, currentUser.getId());
        queryWrapper.eq(StudyClock::getDate, LocalDate.now());
        StudyClock one = this.getOne(queryWrapper);

        ClockSelfDTO clockSelfDTO = new ClockSelfDTO();
        if (one == null) {
            //当日还未签到
            clockSelfDTO.setWhy("今天还未签到打卡哦~");
            clockSelfDTO.setIsStandard(false);
            clockSelfDTO.setIsSigned(false);
            clockSelfDTO.setMinOldTime(studyClockConfig.getMinOldTime());
            //处理日期
            Integer maxFirstTime = studyClockConfig.getMaxFirstTime();
            LocalDateTime now = LocalDateTime.now();
            now.withHour(maxFirstTime);
            now.withMinute(0);
            now.withSecond(0);
            clockSelfDTO.setMaxFirstTime(now);
            return clockSelfDTO;
        }
        clockSelfDTO.setIsSigned(true);
        clockSelfDTO.setIsStandard(true);
        if (one.getOldTime() < studyClockConfig.getMinOldTime()) {
            clockSelfDTO.setIsStandard(false);
            clockSelfDTO.setWhy(" 日学习时长不够哦 ");
        }
        LocalDateTime firstTime = one.getFirstTime();
        if (firstTime.getHour() > studyClockConfig.getMaxFirstTime()) {
            clockSelfDTO.setIsStandard(false);
            clockSelfDTO.setWhy(clockSelfDTO.getWhy() + "|| 签到太晚了吧 ||");
        }
        clockSelfDTO.setMinOldTime(studyClockConfig.getMinOldTime());
        //处理日期
        Integer maxFirstTime = studyClockConfig.getMaxFirstTime();
        LocalDateTime now = LocalDateTime.now();
        now.withHour(maxFirstTime);
        now.withMinute(0);
        now.withSecond(0);
        clockSelfDTO.setMaxFirstTime(now);
        clockSelfDTO.setDate(one.getDate());
        clockSelfDTO.setFirstTime(one.getFirstTime());
        clockSelfDTO.setOldTime(one.getOldTime());
        return clockSelfDTO;
    }

    @Override
    public R<ClockSelfDTO> getClockSelfAll() {
        User currentUser = ThreadLocalUtil.getCurrentUser();
        if (currentUser == null) {
            throw new CustomException("登录失效", Code.DEL_TOKEN);
        }
        //查找当前用户当前月的信息
        List<StudyClock> studyClock = studyClockMapper.getStudyClock(currentUser.getId(), LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0), LocalDateTime.now());
        //找到合格天数
        Integer goodDay = 0;
        Double allTime = 0.0;
        for (StudyClock s :
                studyClock) {
            //计算出该月总时间
            allTime += s.getOldTime();
            if (s.getOldTime() < studyClockConfig.getMinOldTime()) {
                continue;
            }
            LocalDateTime firstTime = s.getFirstTime();
            Integer maxFirstTime = studyClockConfig.getMaxFirstTime();
            //遍历法计算出30天内合格天数
            if (firstTime.getHour() <= maxFirstTime) {
                goodDay += 1;
            }
        }
        ClockSelfDTO clockSelfDTO = this.getClockSelf();
        clockSelfDTO.setIntegrityDay(goodDay);
        clockSelfDTO.setMonthTime(allTime);
        return R.success(clockSelfDTO);
    }


}
