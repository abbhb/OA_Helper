package com.qc.printers.custom.study.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.qc.printers.common.common.Code;
import com.qc.printers.common.common.CustomException;
import com.qc.printers.common.common.MyString;
import com.qc.printers.common.common.R;
import com.qc.printers.common.common.domain.entity.PageData;
import com.qc.printers.common.common.utils.RedisUtils;
import com.qc.printers.common.common.utils.ThreadLocalUtil;
import com.qc.printers.common.config.StudyClockConfig;
import com.qc.printers.common.study.domain.entity.StudyClock;
import com.qc.printers.common.study.mapper.StudyClockMapper;
import com.qc.printers.common.study.service.IStudyClockService;
import com.qc.printers.common.user.domain.entity.User;
import com.qc.printers.custom.study.domain.dto.AddClock30DTO;
import com.qc.printers.custom.study.domain.dto.ClockSelfDTO;
import com.qc.printers.custom.study.domain.vo.AdminDayDataParamsVO;
import com.qc.printers.custom.study.domain.vo.ClockSelfEchartsVO;
import com.qc.printers.custom.study.domain.vo.KeepDayDataVO;
import com.qc.printers.custom.study.service.StudyClockService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@Slf4j
public class StudyClockServiceImpl implements StudyClockService {

    private final StudyClockMapper studyClockMapper;
    @Autowired
    private StudyClockConfig studyClockConfig;

    @Autowired
    private IStudyClockService iStudyClockService;

    public StudyClockServiceImpl(StudyClockMapper studyClockMapper) {
        this.studyClockMapper = studyClockMapper;
    }

    @Transactional
    @Override
    public void addClock30(AddClock30DTO addClock30DTO) {
        User currentUser = ThreadLocalUtil.getCurrentUser();
        if (currentUser == null) {
            throw new CustomException("登录失效", Code.DEL_TOKEN);
        }
        String value = (String) RedisUtils.get(MyString.study_clock + currentUser.getId(), String.class);
        if (StringUtils.isNotEmpty(value)) {
            Long lastTime;
            Long cuttentTime;
            try {
                lastTime = Long.valueOf(value);
                long l = System.currentTimeMillis();
                cuttentTime = l;
            } catch (Exception e) {
                throw new CustomException("作弊检测！！！");
            }
            if (cuttentTime.longValue() - lastTime.longValue() < 27L) {
                throw new CustomException("别想作弊！！！");
            }
        }
        RedisUtils.set(MyString.study_clock + currentUser.getId(), String.valueOf(System.currentTimeMillis()), 30L);
        LambdaQueryWrapper<StudyClock> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(StudyClock::getUserId, currentUser.getId());
        queryWrapper.eq(StudyClock::getDate, LocalDate.now());
        StudyClock one = iStudyClockService.getOne(queryWrapper);
        if (one != null) {
            //累加时间即可！
            LambdaUpdateWrapper<StudyClock> studyClockLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
            studyClockLambdaUpdateWrapper.eq(StudyClock::getId, one.getId());
            studyClockLambdaUpdateWrapper.set(StudyClock::getOldTime, one.getOldTime() + 0.5);
            boolean update = iStudyClockService.update(studyClockLambdaUpdateWrapper);
            if (!update) {
                throw new CustomException("异常，请刷新!!!");
            }
        } else {
            StudyClock studyClock = new StudyClock();
            studyClock.setDate(LocalDate.now());
            studyClock.setOldTime(0.5);
            studyClock.setFirstTime(LocalDateTime.now());
            studyClock.setUserId(currentUser.getId());
            iStudyClockService.save(studyClock);
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
        StudyClock one = iStudyClockService.getOne(queryWrapper);

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
            clockSelfDTO.setWhy(clockSelfDTO.getWhy() + "|| 签到太晚了吧");
        }
        if (firstTime.getHour() <= studyClockConfig.getMaxFirstTime() && one.getOldTime() >= studyClockConfig.getMinOldTime()) {
            clockSelfDTO.setWhy(clockSelfDTO.getWhy() + "今日打卡成功");
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

    @Override
    public R<ClockSelfEchartsVO> getSelfClockEcharts() {
        User currentUser = ThreadLocalUtil.getCurrentUser();
        if (currentUser == null) {
            throw new CustomException("登录失效", Code.DEL_TOKEN);
        }
        //查找当前用户当前月的信息
        List<StudyClock> studyClock = studyClockMapper.getStudyClock(currentUser.getId(), LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0), LocalDateTime.now());
        int currentDays = 0;
        List<String> xTextList = new ArrayList<>();
        List<Double> xValueList = new ArrayList<>();
        // 有7天取7天，没有就全取
        for (StudyClock s :
                studyClock) {
            xValueList.add(s.getOldTime());
            xTextList.add(s.getDate().toString());
            currentDays += 1;
            if (currentDays == 7) {
                break;
            }
        }
        Collections.reverse(xTextList);
        Collections.reverse(xValueList);
        ClockSelfEchartsVO clockSelfEchartsVO = new ClockSelfEchartsVO();
        clockSelfEchartsVO.setXTextList(xTextList);
        clockSelfEchartsVO.setXValueList(xValueList);
        return R.success(clockSelfEchartsVO);

    }

    /*
     * 获取签到日数据
     *
     * @param pageNum   分页之当前多少页
     * @param pageSize  分页之每页多少条
     * @param name      通过名称模糊查询
     * @param date      日期段
     * @param firstTime 签到时间段
     * @param groupId   哪个组
     * @return
     */
    @Override
    public R<PageData<List<KeepDayDataVO>>> getAdminDayData(AdminDayDataParamsVO adminDayDataParamsVO) {
        if (adminDayDataParamsVO == null) {
            throw new CustomException("参数不能太离谱~");
        }
        if (adminDayDataParamsVO.getPageSize() > 100 || adminDayDataParamsVO.getPageNum() == null) {
            throw new CustomException("参数不能太离谱~");
        }

        return null;
    }
    /**
     * 获取admin管理面板日数据
     * @return
     */

}
