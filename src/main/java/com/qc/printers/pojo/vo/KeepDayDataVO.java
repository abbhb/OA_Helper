package com.qc.printers.pojo.vo;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class KeepDayDataVO {
    private String name;

    private String studentId;

    private LocalDate date;

    private LocalDateTime firstTime;

    private Double oldTime;

    private Boolean isStandard;

    private String why;
}
