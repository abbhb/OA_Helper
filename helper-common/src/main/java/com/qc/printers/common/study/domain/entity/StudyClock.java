package com.qc.printers.common.study.domain.entity;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class StudyClock implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;


    private Long userId;

    private LocalDate date;

    private LocalDateTime firstTime;

    private Double oldTime;

}
