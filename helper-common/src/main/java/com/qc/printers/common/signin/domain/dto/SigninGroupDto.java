package com.qc.printers.common.signin.domain.dto;

import com.qc.printers.common.signin.domain.entity.SigninGroup;
import com.qc.printers.common.signin.domain.entity.SigninGroupRule;
import lombok.Data;

@Data
public class SigninGroupDto {
    private SigninGroup signinGroup;

    private SigninGroupRule signinGroupRule;

    /**
     * 仅更新了基本信息
     */
    private Boolean onlyBasic;
}
