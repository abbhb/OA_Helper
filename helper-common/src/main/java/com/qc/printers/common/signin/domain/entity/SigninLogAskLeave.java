package com.qc.printers.common.signin.domain.entity;

import lombok.Data;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class SigninLogAskLeave implements Serializable {
    private Long id;


    @NotNull
    private Long userId;

    /**
     * 请假的起始时间
     */
    @NotNull
    private LocalDateTime startTime;

    /**
     * 请假的结束时间
     */
    @NotNull
    private LocalDateTime endTime;

    private LocalDateTime createTime;
}
