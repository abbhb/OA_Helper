package com.qc.printers.custom.user.domain.vo.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.annotation.Nullable;
import java.io.Serializable;

@AllArgsConstructor
@Builder
@NoArgsConstructor
@Data
public class LoginRes implements Serializable {
    private String token;

    /**
     * 是否需要新窗口打开个一键设置密码的页面
     * 2023/12/5 扩展
     * 目前仅在邮箱验证码登录用上此字段
     */
    @Nullable
    private Integer toSetPassword;

    @Nullable
    private String oneTimeSetPasswordCode;
}
