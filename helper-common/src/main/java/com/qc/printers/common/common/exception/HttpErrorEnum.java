package com.qc.printers.common.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Description: 业务校验异常码
 * Author: <a href="https://github.com/zongzibinbin">abin</a>
 * Date: 2023-03-26
 */
@AllArgsConstructor
@Getter
public enum HttpErrorEnum implements ErrorEnum {
    ACCESS_DENIED(401, "登录失效，请重新登录"),
    ;
    private Integer httpCode;
    private String msg;

    @Override
    public Integer getErrorCode() {
        return httpCode;
    }

    @Override
    public String getErrorMsg() {
        return msg;
    }

//    public void sendHttpError(HttpServletResponse response) throws IOException {
//        response.setStatus(this.getErrorCode());
//        R responseData = R.error(this);
//        response.setContentType(ContentType.JSON.toString(Charsets.UTF_8));
//        response.getWriter().write(JSONUtil.toJsonStr(responseData));
//    }
}
