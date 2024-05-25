package com.qc.printers.common.signin.service;

import com.qc.printers.common.signin.domain.entity.SigninLog;
import com.qc.printers.common.signin.domain.entity.SigninLogCli;
import com.qc.printers.common.signin.domain.resp.SigninGroupDateResp;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface SigninLogService {


    String addSigninlogByDevice(HttpServletRequest request, SigninLog signinLog);

    /**
     * 给一个Long userId 和一个日期例如2024-04-24
     * 找到这个人当天的考勤处理记录,已经确认错过去的班次时间如果没打卡就补充缺勤异常记录（state为3的）返回的包含补充的异常记录
     * 因为一个人只会同时处于一个考勤组，所以这样返回的已经是单个考勤组内的数据，无需再细处理
     * 返回的是该天按班次拍好顺序的记录
     *
     * @return
     */
    List<SigninLogCli> getUserInDateAllLogCli(Long userId, LocalDate date);


    /**
     * 返回某天某用户某班次的某班次是否请假
     * @param userId 用户id
     * @param date 某天的date对象
     * @param bcId 某班次id
     * @param bcCount 第几个班次
     * @return 返回一个布尔类型是否请假
     */
    boolean getUserAskForLeave(Long userId, LocalDate date,Long bcId,Integer bcCount);

    SigninGroupDateResp exportSigninGgroupDate(String groupId, LocalDate date);
}
