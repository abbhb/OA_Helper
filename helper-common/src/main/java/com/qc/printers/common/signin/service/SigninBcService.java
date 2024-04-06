package com.qc.printers.common.signin.service;

import com.qc.printers.common.signin.domain.entity.SigninBc;

import java.util.List;

public interface SigninBcService {

    String addSigninBc(SigninBc signinBc);

    String deleteSigninBc(String id);

    String updateSigninBc(SigninBc signinBc);

    List<SigninBc> listSigninBc();
}
