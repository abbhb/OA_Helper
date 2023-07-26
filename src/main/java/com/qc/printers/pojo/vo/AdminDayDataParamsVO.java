package com.qc.printers.pojo.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class AdminDayDataParamsVO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer pageNum;

    private Integer pageSize;

    private String name;

    /**
     * 签到日期范围
     */
    private String date;

    /**
     * 签到时间范围
     */
    private String firstTime;

    private List<Long> groupId;
}
