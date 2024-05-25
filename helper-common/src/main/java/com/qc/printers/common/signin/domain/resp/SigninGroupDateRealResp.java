package com.qc.printers.common.signin.domain.resp;

import com.qc.printers.common.signin.domain.dto.SigninLogRealYiQianDaoDto;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 最近班次都是当前优先，如果不在打卡时段就是上一班次，没有的话就是初始值
 */
@Data
public class SigninGroupDateRealResp implements Serializable {
    private String kaoqingString;
    /**
     * 最近班次应到人数
     */
    private Integer numberOfPeopleSupposedToCome;

    /**
     * 最近班次实到人数
     */
    private Integer numberOfActualArrival;

    /**
     * 最近班次请假人数
     */
    private Integer numberOfLeave;

    /**
     * 最近班次迟到人数
     */
    private Integer numberOfChiDao;
    /**
     * 最近班次早退人数
     */
    private Integer numberOfZaoTUi;

    /**
     * 最近班次已签到的成员【请假返回tag请假】[迟到早退都返回tag]
     */
    private List<SigninLogRealYiQianDaoDto> yiQianDao;

    /**
     * 最近班次没签到的成员【没请假】
     */
    private List<SigninLogRealYiQianDaoDto> weiQianDao;

    /**
     * 昨日缺勤成员[没请假的]【只统计缺勤，不算早退和迟到】
     */
    private List<SigninLogRealYiQianDaoDto> zuoRiQueQing;

    /**
     * 昨日出勤率，0-100
     */
    private String zuoRiChuQingLv;


}
