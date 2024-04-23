package com.qc.printers.common.signin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ecwid.consul.v1.health.model.HealthService;
import com.qc.printers.common.common.CustomException;
import com.qc.printers.common.common.service.ConsulService;
import com.qc.printers.common.signin.dao.SigninDeviceDao;
import com.qc.printers.common.signin.domain.dto.SigninDeviceDto;
import com.qc.printers.common.signin.domain.entity.SigninDevice;
import com.qc.printers.common.signin.domain.entity.resp.DeviceCheckResp;
import com.qc.printers.common.signin.service.SigninDeviceMangerService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class SigninDeviceMangerServiceImpl implements SigninDeviceMangerService {
    @Autowired
    private SigninDeviceDao signinDeviceDao;

    @Autowired
    private ConsulService consulService;

    @Autowired
    private RestTemplate restTemplate;

    @Override
    public List<SigninDeviceDto> getBindDeviceList() {
        List<SigninDeviceDto> signinDeviceDtos = new ArrayList<>();
        List<SigninDevice> list = signinDeviceDao.list();
        List<HealthService> registeredServices = consulService.getRegisteredServices("signin", true);
        for (int i = 0; i < list.size(); i++) {
            SigninDeviceDto signinDeviceDto = new SigninDeviceDto();
            signinDeviceDtos.add(signinDeviceDto);
            signinDeviceDto.setDeviceId(list.get(i).getId());
            signinDeviceDto.setSecret(list.get(i).getSecret());
            signinDeviceDto.setRemark(list.get(i).getRemark());
            signinDeviceDto.setName(list.get(i).getName());
            signinDeviceDto.setSupport(list.get(i).getSupport());

            int finalI = i;
            Optional<HealthService> first = registeredServices.stream().filter(obj -> obj.getService().getId().equals(list.get(finalI).getId())).findFirst();
            if (first.isEmpty()) {
                signinDeviceDto.setOnline(false);
                continue;
            }
            signinDeviceDto.setOnline(true);
            signinDeviceDto.setPort(first.get().getService().getPort());
            signinDeviceDto.setAddress(first.get().getService().getAddress());
        }
        return signinDeviceDtos;
    }

    @Override
    public List<SigninDeviceDto> getCanBindDeviceList() {
        List<SigninDeviceDto> signinDeviceDtos = new ArrayList<>();
        List<SigninDevice> list = signinDeviceDao.list();
        List<HealthService> registeredServices = consulService.getRegisteredServices("signin", true);
        for (int i = 0; i < registeredServices.size(); i++) {
            int finalI = i;
            Optional<SigninDevice> first = list.stream().filter(obj -> obj.getId().equals(registeredServices.get(finalI).getService().getId())).findFirst();
            if (!first.isEmpty()) {
                continue;
            }
            SigninDeviceDto signinDeviceDto = new SigninDeviceDto();
            signinDeviceDtos.add(signinDeviceDto);
            signinDeviceDto.setDeviceId(registeredServices.get(i).getService().getId());
            signinDeviceDto.setOnline(true);
            signinDeviceDto.setSupport(registeredServices.get(i).getService().getMeta().get("zc"));
        }
        return signinDeviceDtos;
    }

    /**
     * 首次绑定需要连接设备校验密钥
     *
     * @param signinDeviceDto
     * @return
     */
    @Transactional
    @Override
    public String addBindDevice(SigninDeviceDto signinDeviceDto) {
        if (StringUtils.isEmpty(signinDeviceDto.getDeviceId())) {
            throw new CustomException("请提供设备id");
        }
        if (StringUtils.isEmpty(signinDeviceDto.getSecret())) {
            throw new CustomException("请输入设备密钥");
        }
        if (StringUtils.isEmpty(signinDeviceDto.getName())) {
            throw new CustomException("请输入设备名称");
        }
        SigninDevice signinDevice = new SigninDevice();
        signinDevice.setId(signinDeviceDto.getDeviceId());
        signinDevice.setSecret(signinDeviceDto.getSecret());
        signinDevice.setSupport(signinDeviceDto.getSupport());
        signinDevice.setRemark(signinDeviceDto.getRemark());
        signinDevice.setName(signinDeviceDto.getName());
        // 校验密钥
        List<HealthService> registeredServices = consulService.getRegisteredServices("signin", true);
        Optional<HealthService> first = registeredServices.stream().filter(obj -> obj.getService().getId().equals(signinDeviceDto.getDeviceId())).findFirst();
        if (first.isEmpty()) {
            throw new CustomException("请保持设备的连接稳定再尝试绑定!");
        }
        String reqUrl = "http://" + first.get().getService().getAddress() + ":" + first.get().getService().getPort() + "/check_secert?secert=" + signinDeviceDto.getSecret();
        DeviceCheckResp response = restTemplate.getForObject(reqUrl, DeviceCheckResp.class);
        assert response != null;
        if (!response.getCode().equals(1)) {
            throw new CustomException("验证密钥失败，请检查密钥并重试!");
        }
        signinDeviceDao.save(signinDevice);
        return "绑定成功";
    }

    @Transactional
    @Override
    public boolean checkDevice(String signinDeviceId, String signinSecret) {
        LambdaQueryWrapper<SigninDevice> signinDeviceLambdaQueryWrapper = new LambdaQueryWrapper<>();
        signinDeviceLambdaQueryWrapper.eq(SigninDevice::getId, signinDeviceId);
        SigninDevice one = signinDeviceDao.getOne(signinDeviceLambdaQueryWrapper);
        if (one == null) throw new CustomException("对象异常");
        if (!one.getSecret().equals(signinSecret)) {
            throw new CustomException("鉴权失败");
        }
        return true;
    }
}
