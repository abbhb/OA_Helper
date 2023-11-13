package com.qc.printers.common.oauth.exception;

import com.qc.printers.common.oauth.domain.enums.OauthErrorEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class OauthException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    /**
     * 错误码
     */
    protected Integer errorCode;

    /**
     * 错误信息
     */
    protected String errorMsg;

    public OauthException() {
        super();
    }

    public OauthException(String errorMsg) {
        super(errorMsg);
        this.errorMsg = errorMsg;
        this.errorCode = OauthErrorEnum.OAUTH_ERROR.getErrorCode();
    }

    public OauthException(OauthErrorEnum error) {
        super(error.getErrorMsg());
        this.errorCode = error.getErrorCode();
        this.errorMsg = error.getErrorMsg();
    }

    public OauthException(OauthErrorEnum error, String errorMsg) {
        super(error.getErrorMsg());
        this.errorCode = error.getErrorCode();
        this.errorMsg = errorMsg;
    }
}
