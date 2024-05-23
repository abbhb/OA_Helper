package com.qc.printers.custom.signin.service;

import com.qc.printers.common.signin.domain.dto.SigninDeviceDto;
import com.qc.printers.custom.signin.domain.req.SigninUserFaceDataReq;
import com.qc.printers.custom.signin.domain.vo.SigninUserFaceDataResp;

import java.util.List;

public interface SigninUserDataService {
    List<SigninUserFaceDataResp> getSigninFaceData(String deviceId);

    String uploadSigninFaceData(SigninUserFaceDataReq signinUserFaceDataReq);

    SigninDeviceDto checkDeviceStatus(String deviceId, String needType);
}
