package com.qc.printers.common.signin.domain.entity;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
public class BcRule implements Serializable {


    private Integer count;

    /**
     * 上班时间
     */
    private String sbTime;

    /**
     * 上班是否需要打卡
     *  暂时必须打卡
     */
//    private Boolean sbNeedSignin;

    /**
     * 计算打卡时间段必须按照顺序，且不能重合时间段
     */

    /**
     * 上班前多少分钟打卡
     */
    private Integer sbStartTime;
    /**
     * 上班后多少分钟
     */
    private Integer sbEndTime;

    /**
     * 下班时间
     * 日期无效，只取时间
     */
    private String xbTime;

    /**
     * 下班前多少分钟打卡
     */
    private Integer xbStartTime;

    /**
     * 下班后多少分钟
     */
    private Integer xbEndTime;

    /**
     * 最后一班下班时间是否是次日
     */
    @ApiModelProperty("暂时不生效，该配置，不支持跨日")
    private Integer ciRi;

    private Boolean lianban;// 下一班次的上班免打卡
    /**
     * 下班是否需要打卡
     * 暂时必须打卡
     */
//    private Boolean xbNeedSignin;


}
