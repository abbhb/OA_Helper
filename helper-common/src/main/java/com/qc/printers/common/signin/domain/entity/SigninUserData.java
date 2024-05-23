package com.qc.printers.common.signin.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class SigninUserData implements Serializable {
    @TableId
    private Long id;

    private Long userId;

    /**
     * 目前仅有face的设备
     */
    private String faceData;

    private LocalDateTime updateTime;
}
