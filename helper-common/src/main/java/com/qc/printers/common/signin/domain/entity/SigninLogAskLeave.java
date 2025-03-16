package com.qc.printers.common.signin.domain.entity;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
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

    /**
     * 请假理由
     */
    private String askLeaveReason;

    /**
     * 关联单据id，有就带着
     */
    private String askLeaveAboutActId;

    private LocalDateTime createTime;

    /**
     * 请假类型
     */
    private String askLeaveLeaveType;
}
