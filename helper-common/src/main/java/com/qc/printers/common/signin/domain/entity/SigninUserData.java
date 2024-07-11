package com.qc.printers.common.signin.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.qc.printers.common.common.annotation.Excel;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class SigninUserData implements Serializable {

    @TableId
    private Long id;

    @Excel(name = "用户编号", width = 30, cellType = Excel.ColumnType.STRING, prompt = "用户编号")
    private Long userId;

    /**
     * 目前仅有face的设备
     */
    @Excel(name = "人脸数据[欧式距离算法生成勿手动修改]", type = Excel.Type.EXPORT,cellType = Excel.ColumnType.STRING, prompt = "人脸数据")
    private String faceData;

    /**
     * 第二种 card
     */
    @Excel(name = "ID卡号", cellType = Excel.ColumnType.STRING, prompt = "ID卡号")
    private String cardId;

    @Excel(name = "最后更新时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss", type = Excel.Type.EXPORT)
    private LocalDateTime updateTime;
}
