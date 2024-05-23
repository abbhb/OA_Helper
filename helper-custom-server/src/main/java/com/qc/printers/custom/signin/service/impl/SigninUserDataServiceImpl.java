package com.qc.printers.custom.signin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ecwid.consul.v1.health.model.HealthService;
import com.qc.printers.common.common.CustomException;
import com.qc.printers.common.common.service.ConsulService;
import com.qc.printers.common.signin.dao.SigninDeviceDao;
import com.qc.printers.common.signin.dao.SigninUserDataDao;
import com.qc.printers.common.signin.domain.dto.SigninDeviceDto;
import com.qc.printers.common.signin.domain.entity.SigninDevice;
import com.qc.printers.common.signin.domain.entity.SigninUserData;
import com.qc.printers.common.signin.domain.resp.PythonServerResp;
import com.qc.printers.common.signin.service.SigninUserDataMangerService;
import com.qc.printers.common.user.dao.UserDao;
import com.qc.printers.common.user.domain.entity.SysDept;
import com.qc.printers.common.user.domain.entity.User;
import com.qc.printers.common.user.service.ISysDeptService;
import com.qc.printers.custom.signin.domain.dto.SigninUserFaceDataDto;
import com.qc.printers.custom.signin.domain.req.SigninUserFaceDataReq;
import com.qc.printers.custom.signin.domain.vo.SigninUserFaceDataResp;
import com.qc.printers.custom.signin.service.SigninUserDataService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SigninUserDataServiceImpl implements SigninUserDataService {
    private final RestTemplate restTemplate;

    @Autowired
    private ConsulService consulService;

    @Autowired
    private UserDao userDao;

    @Autowired
    private SigninUserDataMangerService signinUserDataMangerService;

    @Autowired
    private ISysDeptService iSysDeptService;

    @Autowired
    private SigninUserDataDao signinUserDataDao;

    @Autowired
    private SigninDeviceDao signinDeviceDao;


    public SigninUserDataServiceImpl(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public List<SigninUserFaceDataResp> getSigninFaceData(String deviceId) {
        SigninDeviceDto signinDevice = this.checkDeviceStatus(deviceId, "face");
        // 设置请求头
        HttpHeaders headers = new HttpHeaders();
        headers.set("secert_h", signinDevice.getSecret());

        // 创建 HttpEntity，并传入请求头
        HttpEntity<String> entity = new HttpEntity<>(headers);
        String url = "http://" + signinDevice.getAddress() + ":" + signinDevice.getPort() + "/list_all_face";
        ResponseEntity<List<SigninUserFaceDataDto>> exchange = restTemplate.exchange(url, HttpMethod.GET, entity, new ParameterizedTypeReference<List<SigninUserFaceDataDto>>() {
        });
        List<SigninUserFaceDataDto> body = exchange.getBody();
        List<User> userList = userDao.list();
        List<SigninUserFaceDataResp> signinUserFaceDataResps = new ArrayList<>();
        // 将 List<Item> 转换为 Map<Integer, Item>
        Map<String, SigninUserFaceDataDto> stringSigninUserFaceDataDtoMap = body.stream()
                .collect(Collectors.toMap(SigninUserFaceDataDto::getStudentId, item -> item));
        for (User user1 : userList) {
            SigninUserFaceDataResp signinUserFaceDataResp = new SigninUserFaceDataResp();
            SysDept sysDept = iSysDeptService.getById(user1.getDeptId());
            //从部门继承的不能直接显示，或者需要却别开，不然很乱
            LambdaQueryWrapper<SigninUserData> signinUserDataLambdaQueryWrapper = new LambdaQueryWrapper<>();
            signinUserDataLambdaQueryWrapper.eq(SigninUserData::getUserId, user1.getId());
            SigninUserData one = signinUserDataDao.getOne(signinUserDataLambdaQueryWrapper);
            signinUserFaceDataResp.setLocalExist(true);
            if (one == null || StringUtils.isEmpty(one.getFaceData())) {
                // 人脸里面真的有数据就是存在,不存在都设置为NULL
                signinUserFaceDataResp.setLocalExist(false);
            }
            signinUserFaceDataResp.setUserId(user1.getId());
            signinUserFaceDataResp.setName(user1.getName());
            signinUserFaceDataResp.setUsername(user1.getUsername());
            signinUserFaceDataResp.setDeptId(user1.getDeptId());
            signinUserFaceDataResp.setDeptName(sysDept.getDeptNameAll());
            signinUserFaceDataResp.setStudentId(user1.getStudentId());
            signinUserFaceDataResp.setDeviceExist(false);
            if (stringSigninUserFaceDataDtoMap.get(user1.getStudentId()) != null) {
                signinUserFaceDataResp.setDeviceExist(true);
            }
            signinUserFaceDataResps.add(signinUserFaceDataResp);
        }
        return signinUserFaceDataResps;
    }

    @Transactional
    @Override
    public String uploadSigninFaceData(SigninUserFaceDataReq signinUserFaceDataReq) {
        SigninDeviceDto signinDevice = this.checkDeviceStatus(signinUserFaceDataReq.getDeviceId(), "face");

        if (!signinDevice.getOnline()) {
            throw new CustomException("设备掉线，同步失败");
        }

        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");

        headers.set("secert_h", signinDevice.getSecret());
        String url = "http://" + signinDevice.getAddress() + ":" + signinDevice.getPort() + "/sync_upload";
        Map<String, Object> requestBody = new HashMap<>();

        // data处理
        List<Map<String, Object>> datas = new ArrayList<>();
        List<SigninUserFaceDataResp> data = signinUserFaceDataReq.getData();
        for (SigninUserFaceDataResp datum : data) {
            Map<String, Object> everyP = new HashMap<>();
            User byId = userDao.getById(datum.getUserId());
            if (byId == null) {
                throw new CustomException(datum.getUsername() + "用户不存在");
            }
            everyP.put("username", byId.getName());
            everyP.put("student_id", byId.getStudentId());
            if (datum.isLocalExist()) {
                LambdaQueryWrapper<SigninUserData> objectLambdaQueryWrapper = new LambdaQueryWrapper<>();
                objectLambdaQueryWrapper.eq(SigninUserData::getUserId, byId.getId());
                SigninUserData one = signinUserDataDao.getOne(objectLambdaQueryWrapper);
                if (one == null) {
                    throw new CustomException("请重试，系统数据发生了变更");
                }
                everyP.put("face_data", one.getFaceData());
                everyP.put("is_none", false);

            } else {
                everyP.put("is_none", true);
            }
            datas.add(everyP);

        }
        requestBody.put("model", signinUserFaceDataReq.getSyncModel());
        requestBody.put("data", datas);
        // 将Headers和请求体封装到HttpEntity中
        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

        PythonServerResp pythonServerResp = restTemplate.postForObject(url, requestEntity, PythonServerResp.class);
        if (pythonServerResp == null) {
            throw new CustomException("无响应");
        }
        if (!pythonServerResp.getCode().equals(1)) {
            throw new CustomException(pythonServerResp.getMsg());
        }
        return "同步成功";
    }


    @Override
    public SigninDeviceDto checkDeviceStatus(String deviceId, String needType) {
        List<HealthService> registeredServices = consulService.getRegisteredServices("signin", true);
        Optional<HealthService> first = registeredServices.stream().filter(obj -> obj.getService().getId().equals(deviceId)).findFirst();
        if (first.isEmpty()) {
            throw new CustomException("设备掉线");
        }
        HealthService.Service service = first.get().getService();
        String s = service.getMeta().get("zc");
        if (!s.contains(needType)) {
            throw new CustomException("该设备本身就不支持" + needType);
        }
        SigninDevice signinDevice = signinDeviceDao.getById(deviceId);
        if (signinDevice == null) {
            throw new CustomException("设备异常");
        }
        SigninDeviceDto signinDeviceDto = new SigninDeviceDto();
        BeanUtils.copyProperties(signinDevice, signinDeviceDto);
        signinDeviceDto.setAddress(service.getAddress());
        signinDeviceDto.setPort(service.getPort());
        return signinDeviceDto;
    }
}
