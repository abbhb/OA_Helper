package com.qc.printers.common.signin.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;

@Data
public class UserExtFace implements Serializable {

    @TableId
    private String faceId;

    private Long userId;

    /**
     * 原始人脸图，只有直接上传的才会有
     */
    private String faceYImg;

    private String faceSImg;

    /**
     * 只有从打卡设备同步过来的数据才会有这条
     */
    private String faceRange;
}
