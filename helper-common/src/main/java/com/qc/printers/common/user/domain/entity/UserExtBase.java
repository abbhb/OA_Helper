package com.qc.printers.common.user.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.io.Serializable;

@Data
public class UserExtBase implements Serializable {

    @TableId
    private Long id;               // 用户id，每个用户只能存在一条，直接作为主键即可
    @NotEmpty(message = "证件照不能为空")
    private String idPhoto;        // 证件照，可以为空
    private String csd1;           // 出生地省
    private String csd2;           // 出生地市
    private String csd3;           // 出生地区
    private String jg1;            // 籍贯省
    private String jg2;            // 籍贯市
    private String jg3;            // 籍贯区
    private String syd1;           // 生源地省
    private String syd2;           // 生源地市
    private String zzmm;           // 政治面貌
    private String mz;             // 民族
    @NotEmpty(message = "详细地址不能为空")
    private String detailAddress; // 详细地址
    @NotEmpty(message = "身份证件类型不能为空")
    private String sfzLx;      // 身份证件类型
    @NotEmpty(message = "身份证号不能为空")
    private String sfzId;      // 身份证号
    private String csrq;           // 出生日期，格式为2000-01-01
    @NotEmpty(message = "真实姓名不能为空")
    private String zsxm;           // 真实姓名
}
