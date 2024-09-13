package com.qc.printers.common.signin.domain.resp;

import com.qc.printers.common.signin.domain.dto.SigninLogCliBcDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;


/**
 * 列表页单天展示对象（不包含详情，具体后续补接口即可）
 * 统一通过一个个人id和date进入某个具体的个人详情页，此处列表和管理员的全局列表都公用详情
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SigninLogForSelfResp implements Serializable {
    // 某一天
    private String currentDate;

    /**
     * 当天存在几班
     */
    private Integer bcCount;

    /**
     * 异常原因，无则空，append往后添加即可
     */
    private String errMsg;

    /**
     * 当天各班次详细情况
     */
    private List<SigninLogCliBcDto> bcDetail;


    /**
     * 首次打卡时间
     */
    private String firstTime;

    /**
     * 末打卡时间
     */
    private String endTime;


    /**
     * 实际工时，精确到每个班次，每个班次下班的最晚卡如果超过下班加上额外下班时，上班就是早于，添加，迟到早退更好计算，对应的扣除缺勤时间
     */
    private String workingHours;

    /**
     * 缺勤时长
     */
    private String queQinTime;

}
