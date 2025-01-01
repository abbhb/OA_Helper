package com.qc.printers.common.signin.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.ecwid.consul.v1.health.model.HealthService;
import com.qc.printers.common.common.service.CommonService;
import com.qc.printers.common.common.service.ConsulService;
import com.qc.printers.common.signin.dao.SigninUserDataDao;
import com.qc.printers.common.signin.domain.entity.SigninUserData;
import com.qc.printers.common.signin.domain.resp.FaceDataResp;
import com.qc.printers.common.signin.service.SigninUserDataMangerService;
import com.qc.printers.transaction.annotation.SecureInvoke;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class SigninUserDataMangerServiceImpl implements SigninUserDataMangerService {
    private final RestTemplate restTemplate;
    @Autowired
    private CommonService commonService;
    @Autowired
    private ConsulService consulService;
    @Autowired
    private SigninUserDataDao signinUserDataDao;

    public SigninUserDataMangerServiceImpl(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    // 异步执行,默认线程池
    @Transactional
    @SecureInvoke
    @Override
    public void UpdateUserFaceByUser(Long userId, String faceFileKey) {
        String allImageUrl = commonService.getAllImageUrl(faceFileKey);
        if (!allImageUrl.startsWith("http")) {
            throw new RuntimeException("不是http");
        }
        List<HealthService> registeredServices = consulService.getFace2ArrayServices();
        if (registeredServices.size() == 0) {
            throw new RuntimeException("没有转换服务存活请检查docker");
        }
        JSONObject reqjson = new JSONObject();
        reqjson.put("url", allImageUrl);
        String url = "http://" + registeredServices.get(0).getService().getAddress() + ":" + registeredServices.get(0).getService().getPort() + "/face_by_oss/";
        // 创建HttpEntity实例
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(reqjson.toJSONString(), headers);

        FaceDataResp faceDataResp = restTemplate.postForObject(url, entity, FaceDataResp.class);
        if (!faceDataResp.getCode().equals(1)) {
            throw new RuntimeException("转换完状态不为1");
        }
        LambdaQueryWrapper<SigninUserData> signinUserDataLambdaQueryWrapper = new LambdaQueryWrapper<>();
        signinUserDataLambdaQueryWrapper.eq(SigninUserData::getUserId, userId);
        int count = signinUserDataDao.count(signinUserDataLambdaQueryWrapper);
        if (count > 0) {
            LambdaUpdateWrapper<SigninUserData> signinUserDataLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
            signinUserDataLambdaUpdateWrapper.eq(SigninUserData::getUserId, userId);
            signinUserDataLambdaUpdateWrapper.set(SigninUserData::getFaceData, faceDataResp.getFaceData());
            signinUserDataLambdaUpdateWrapper.set(SigninUserData::getUpdateTime, LocalDateTime.now());
            signinUserDataDao.update(signinUserDataLambdaUpdateWrapper);
            return;
        }
        SigninUserData signinUserData = new SigninUserData();
        signinUserData.setFaceData(faceDataResp.getFaceData());
        signinUserData.setUserId(userId);
        signinUserData.setUpdateTime(LocalDateTime.now());
        signinUserDataDao.save(signinUserData);

    }
}
