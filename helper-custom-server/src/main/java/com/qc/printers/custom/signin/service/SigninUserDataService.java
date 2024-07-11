package com.qc.printers.custom.signin.service;

import com.qc.printers.common.common.domain.entity.PageData;
import com.qc.printers.common.signin.domain.dto.SigninDeviceDto;
import com.qc.printers.common.signin.domain.dto.SigninUserDataExcelDto;
import com.qc.printers.custom.signin.domain.req.SigninUserCardDataReq;
import com.qc.printers.custom.signin.domain.req.SigninUserFaceDataReq;
import com.qc.printers.custom.signin.domain.vo.SigninDataResp;
import com.qc.printers.custom.signin.domain.vo.SigninUserCardDataResp;
import com.qc.printers.custom.signin.domain.vo.SigninUserFaceDataResp;

import java.util.List;

public interface SigninUserDataService {
    /**
     * Face相关
     * @param deviceId
     * @return
     */
    List<SigninUserFaceDataResp> getSigninFaceData(String deviceId);

    String uploadSigninFaceData(SigninUserFaceDataReq signinUserFaceDataReq);

    String downloadSigninFaceData(SigninUserFaceDataReq signinUserFaceDataReq);


    /**
     * Card相关
     * @param deviceId
     * @return
     */
    List<SigninUserCardDataResp> getSigninCardData(String deviceId);

    String uploadSigninCardData(SigninUserCardDataReq signinUserCardDataReq);

    String downloadSigninCardData(SigninUserCardDataReq signinUserCardDataReq);


    /**
     * 公共
     * @param deviceId
     * @param needType
     * @return
     */
    SigninDeviceDto checkDeviceStatus(String deviceId, String needType);

    PageData<SigninDataResp> getDataMangerList(Integer pageNum, Integer pageSize, String name, Integer cascade, Long deptId);

    List<SigninUserDataExcelDto> exportAllData();

    String importSigninUserCardData(List<SigninUserDataExcelDto> dataList, boolean updateSupport);
}
