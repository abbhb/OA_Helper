package com.qc.printers.common.common;

import com.qc.printers.common.common.exception.BusinessException;
import com.qc.printers.common.common.exception.FrequencyControlException;
import com.qc.printers.common.oauth.domain.dto.OauthBase;
import com.qc.printers.common.oauth.exception.OauthException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.sql.SQLIntegrityConstraintViolationException;

/**
 * 全局异常处理器
 * 捕获Controller层的异常
 */
@ControllerAdvice(annotations = {RestController.class, Controller.class})
@ResponseBody
@Slf4j
public class GlobalExceptionHandler {
    /**
     * 异常处理方法
     * @return
     */
    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    public R<String> exceptionHandler(SQLIntegrityConstraintViolationException ex){
        log.error(ex.getMessage());

        if (ex.getMessage().contains("Duplicate entry")) {//设置过约束，某个值唯一的话
            String[] split = ex.getMessage().split(" ");
            String msg = split[2] + "已存在";
            return R.error(msg);
        }

        return R.error("未知错误");
    }

    @ExceptionHandler(OauthException.class)
    public OauthBase oauthExceptionHandler(OauthException e, HttpServletResponse response) {
        OauthBase oauthBase = new OauthBase();
        oauthBase.setCode(e.getErrorCode());
        oauthBase.setMsg(e.getErrorMsg());
        return oauthBase;
    }

    @ExceptionHandler(CustomException.class)
    public R<String> customExceptionHandler(CustomException e, HttpServletResponse response) {
        if (e.getCode() != null) {
            if (e.getCode().equals(Code.DEL_TOKEN)) {
                //token过期了
                return R.error(Code.DEL_TOKEN, e.getMessage());
            }
        }
        return R.error(e.getMessage());
    }

    @ExceptionHandler(FrequencyControlException.class)
    public R<String> frequencyControlExceptionHandler(FrequencyControlException e, HttpServletResponse response) {
        return R.error(e.getErrorMsg());
    }
    @ExceptionHandler(BusinessException.class)
    public R<String> businessExceptionHandler(BusinessException e, HttpServletResponse response) {
        return R.error(e.getErrorMsg());
    }

    /**
     * 捕获  RuntimeException 异常
     * TODO  如果你觉得在一个 exceptionHandler 通过  if (e instanceof xxxException) 太麻烦
     * TODO  那么你还可以自己写多个不同的 exceptionHandler 处理不同异常
     */
    @ExceptionHandler(RuntimeException.class)
    public R<String> runtimeExceptionHandler(Exception e) {
        if (e.getCause() instanceof SQLIntegrityConstraintViolationException) {
//            if(e.getMessage().contains("Duplicate entry")){//设置过约束，某个值唯一的话
//                String[] split = e.getMessage().split(" ");
//
//                String msg = split[2] + "已存在";
//                log.info("{}",split);
//                return R.error(msg);
//            }
            return R.error("请注意,用户名等数据不能重复,有数据重复了");
        } else if (e.getCause() instanceof RuntimeException) {
            log.error(e.getMessage(),e.getClass());
            return R.error("运行异常");
        }
        e.printStackTrace();
        return R.error("运行异常");
    }

    @ExceptionHandler(NullPointerException.class)
    public R<String> nullPointerExceptionHandler(NullPointerException e) {
        log.error(e.getMessage());
        return R.error("参数异常");
    }
}
