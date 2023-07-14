package com.qc.printers.pojo;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class StudyClock {
    private static final long serialVersionUID = 1L;

    private Long id;


    private Long userId;

    private LocalDate date;

    private LocalDateTime firstTime;

    private Double oldTime;

}
