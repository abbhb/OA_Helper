package com.qc.printers.common.oauth.domain.enums;

import com.qc.printers.common.common.exception.ErrorEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum OauthErrorEnum implements ErrorEnum {
    OAUTH_PERMISSION_BUQUAN(601, "Oauth需要补充授权"),
    OAUTH_NEED_AUTHORIZATION(602, "Oauth需要授权"),
    OAUTH_ERROR(603, "未知的oauth异常，请联系管理员"),
    ;

    private final Integer code;
    private final String msg;

    @Override
    public Integer getErrorCode() {
        return null;
    }

    @Override
    public String getErrorMsg() {
        return null;
    }
}
