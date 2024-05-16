package com.qc.printers.common.signin.service;

import com.qc.printers.common.signin.domain.dto.SigninDeviceDto;

import java.util.List;

public interface SigninDeviceMangerService {

    List<SigninDeviceDto> getBindDeviceList();

    /**
     * 返回为绑定但已上线设备
     *
     * @return
     */
    List<SigninDeviceDto> getCanBindDeviceList();

    /**
     * 需要传入id
     *
     * @param signinDeviceDto
     * @return
     */
    String addBindDevice(SigninDeviceDto signinDeviceDto);

    boolean checkDevice(String signinDeviceId, String signinSecret);

    String updateBindDeviceBasic(SigninDeviceDto signinDeviceDto);

    void deviceDevice(String deviceId);
}
