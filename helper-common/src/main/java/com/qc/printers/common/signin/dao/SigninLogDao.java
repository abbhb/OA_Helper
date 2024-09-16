package com.qc.printers.common.signin.dao;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qc.printers.common.signin.domain.entity.SigninLog;
import com.qc.printers.common.signin.mapper.SigninLogMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Service
@Transactional
public class SigninLogDao extends ServiceImpl<SigninLogMapper, SigninLog> {


    public SigninLog getLastLogDayByUserId(Long userId, LocalDate date){
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);
        return lambdaQuery().eq(SigninLog::getUserId, userId)
                .between(SigninLog::getSigninTime, startOfDay, endOfDay)
                .orderByDesc(SigninLog::getSigninTime)
                .last("LIMIT 1")
                .one();
    }

    public SigninLog getFirstLogDayByUserId(Long userId, LocalDate date){
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);
        return lambdaQuery().eq(SigninLog::getUserId, userId)
                .between(SigninLog::getSigninTime, startOfDay, endOfDay)
                .orderByAsc(SigninLog::getSigninTime)
                .last("LIMIT 1")
                .one();
    }
}
