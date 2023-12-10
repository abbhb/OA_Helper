package com.qc.printers.custom.user.service;


import com.qc.printers.common.common.R;
import com.qc.printers.custom.user.domain.vo.request.ThirdFirstLoginReq;
import com.qc.printers.custom.user.domain.vo.response.LoginRes;
import com.qc.printers.custom.user.domain.vo.response.ThirdCallbackResp;

public interface TrLoginService {

    R<LoginRes> casLogin(String code);

    ThirdCallbackResp uniCallback(String type, String code);


    LoginRes uniFirstLogin(ThirdFirstLoginReq thirdFirstLoginReq);
}
