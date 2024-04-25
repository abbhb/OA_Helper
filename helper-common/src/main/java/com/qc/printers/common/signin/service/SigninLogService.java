package com.qc.printers.common.signin.service;

import com.qc.printers.common.signin.domain.entity.SigninLog;

import javax.servlet.http.HttpServletRequest;

public interface SigninLogService {


    String addSigninlogByDevice(HttpServletRequest request, SigninLog signinLog);
}
