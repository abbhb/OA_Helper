package com.qc.printers.common.signin.domain.resp;

import com.qc.printers.common.signin.domain.dto.SigninGroupDateUserDto;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class SigninGroupDateResp implements Serializable {
//    /**
//     * 应到人数
//     */
//    private Integer numberOfPeopleSupposedToCome;
//
//    /**
//     * 实到人数
//     */
//    private Integer numberOfActualArrival;
//
//    /**
//     * 请假人数
//     */
//    private Integer numberOfLeave;
    //这些只能在实时情况下计算
    /**
     * 今日是否需要考勤
     */
    private Boolean atendanceRequired;
    /**
     * 应该出勤人数
     */
    private Integer numberOfPeopleSupposedToCome;

    /**
     * 完成应该出勤人数[包含请假，二改状态等等最终完成了即可]
     */
    private Integer numberOfFullAttendance;

    /**
     * 出勤异常人数
     */
    private Integer numberOfError;

    /**
     * 异常拷贝一份
     */
    private List<SigninGroupDateUserDto> userErrorLogList;


    /**
     * 用户详细情况
     */
    private List<SigninGroupDateUserDto> userLogList;



}
