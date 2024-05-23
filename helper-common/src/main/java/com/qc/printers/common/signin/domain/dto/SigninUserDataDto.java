package com.qc.printers.common.signin.domain.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 人脸同步按钮页，如果后期加指纹，根据support会有不同功能的同步页
 */
@Data
public class SigninUserDataDto implements Serializable {

    private Long id;

    private Long userId;

    private String faceData;

    private LocalDateTime updateTime;

    /**
     * 用户昵称
     */
    private String name;

    private String studentId;
}
