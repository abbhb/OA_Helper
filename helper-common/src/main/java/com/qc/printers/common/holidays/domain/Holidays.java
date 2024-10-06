package com.qc.printers.common.holidays.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * 节假日
 */
@Data
public class Holidays implements Serializable {
    private Long id;
    /**
     * 属于哪个考勤组下
     */
    private Long signinGroupId;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private LocalDate dateD;

    private String name;


    /**
     * 0：真实节假日放假
     * 1：节假日需要上班
     */
    private Integer workingDay;
}
