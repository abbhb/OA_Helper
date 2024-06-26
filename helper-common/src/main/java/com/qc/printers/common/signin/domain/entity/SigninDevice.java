package com.qc.printers.common.signin.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;

@Data
public class SigninDevice implements Serializable {

    @TableId()
    private String id;

    /**
     * 密钥
     */
    private String secret;
    private String name;
    private String support;
    private String remark;

}
