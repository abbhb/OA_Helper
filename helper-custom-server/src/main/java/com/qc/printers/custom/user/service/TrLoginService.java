package com.qc.printers.custom.user.service;


import com.qc.printers.common.common.R;
import com.qc.printers.custom.user.domain.vo.response.LoginRes;

public interface TrLoginService {

    R<LoginRes> casLogin(String code);

}
