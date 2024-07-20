package com.qc.printers.custom.signin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ecwid.consul.v1.health.model.HealthService;
import com.qc.printers.common.common.CustomException;
import com.qc.printers.common.common.domain.entity.PageData;
import com.qc.printers.common.common.service.ConsulService;
import com.qc.printers.common.common.utils.AssertUtil;
import com.qc.printers.common.common.utils.oss.OssDBUtil;
import com.qc.printers.common.common.utils.poi.ExcelUtil;
import com.qc.printers.common.config.MinIoProperties;
import com.qc.printers.common.signin.dao.SigninDeviceDao;
import com.qc.printers.common.signin.dao.SigninUserDataDao;
import com.qc.printers.common.signin.domain.dto.SigninDeviceDto;
import com.qc.printers.common.signin.domain.dto.UserDataImportErrorDto;
import com.qc.printers.common.signin.domain.dto.SigninUserDataExcelDto;
import com.qc.printers.common.signin.domain.entity.SigninDevice;
import com.qc.printers.common.signin.domain.entity.SigninUserData;
import com.qc.printers.common.signin.domain.resp.PythonServerResp;
import com.qc.printers.common.signin.service.SigninUserDataMangerService;
import com.qc.printers.common.user.dao.UserDao;
import com.qc.printers.common.user.domain.dto.DeptManger;
import com.qc.printers.common.user.domain.entity.SysDept;
import com.qc.printers.common.user.domain.entity.User;
import com.qc.printers.common.user.service.ISysDeptService;
import com.qc.printers.custom.signin.domain.dto.SigninUserCardDataDto;
import com.qc.printers.custom.signin.domain.dto.SigninUserFaceDataDto;
import com.qc.printers.custom.signin.domain.req.SigninUserCardDataReq;
import com.qc.printers.custom.signin.domain.req.SigninUserFaceDataReq;
import com.qc.printers.custom.signin.domain.vo.SigninDataResp;
import com.qc.printers.custom.signin.domain.vo.SigninUserCardDataResp;
import com.qc.printers.custom.signin.domain.vo.SigninUserFaceDataResp;
import com.qc.printers.custom.signin.service.SigninUserDataService;
import com.qc.printers.custom.user.service.DeptService;
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

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
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
    private DeptService deptService;

    @Autowired
    private SigninUserDataMangerService signinUserDataMangerService;

    @Autowired
    private ISysDeptService iSysDeptService;

    @Autowired
    private MinIoProperties minIoProperties;

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
        log.info("device:{},face{}", deviceId, body);
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
        signinDeviceDto.setOnline(true);

        signinDeviceDto.setAddress(service.getAddress());
        signinDeviceDto.setPort(service.getPort());
        return signinDeviceDto;
    }

    @Override
    public PageData<SigninDataResp> getDataMangerList(Integer pageNum, Integer pageSize, String name, Integer cascade, Long deptId) {
        if (pageNum == null) {
            throw new IllegalArgumentException("传参错误");
        }
        if (pageSize == null) {
            throw new IllegalArgumentException("传参错误");
        }
        if (cascade == null) {
            cascade = 0;// 0为不级联，1为级联
        }
        Page<User> pageInfo = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<User> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        //添加过滤条件
        lambdaQueryWrapper.like(StringUtils.isNotEmpty(name), User::getName, name);
        AssertUtil.notEqual(deptId, null, "请指定查询");
        if (!cascade.equals(1)) {
            lambdaQueryWrapper.eq(User::getDeptId, deptId);
        } else {
            List<DeptManger> deptMangers = deptService.getDeptListOnlyTree();
            Set<DeptManger> childTemp = new HashSet<>();
            Set<Long> childId = new HashSet<>();
            int isDeptIdOrChild = 0;
            while (deptMangers != null && deptMangers.size() > 0) {
                for (DeptManger det :
                        deptMangers) {
                    if (det == null) {
                        continue;
                    }
                    if (det.getChildren() != null && det.getChildren().size() > 0) {
                        childTemp.addAll(det.getChildren());
                    }
                    if (det.getId().equals(deptId)) {
                        if (det.getChildren() != null) {
                            childTemp = new HashSet<>(det.getChildren());
                        }
                        isDeptIdOrChild = 1;
                        childId.add(det.getId());
                        break;
                    }
                    if (isDeptIdOrChild == 1) {
                        childId.add(det.getId());
                    }
                }
                if (childTemp.size() == 0) {
                    break;
                }
                deptMangers = new ArrayList<>(childTemp);

                childTemp = new HashSet<>();
            }
            if (childId.size() > 0) {
                log.info("childId={}", childId);
                lambdaQueryWrapper.in(User::getDeptId, childId);
            }
        }
        //添加排序条件
        lambdaQueryWrapper.orderByAsc(User::getCreateTime);//按照创建时间排序
        userDao.page(pageInfo, lambdaQueryWrapper);
        PageData<SigninDataResp> pageData = new PageData<>();
        List<SigninDataResp> results = new ArrayList<>();
        Set<Long> uids = pageInfo.getRecords().stream().map(User::getId).collect(Collectors.toSet());
        LambdaQueryWrapper<SigninUserData> signinUserDataLambdaQueryWrapper = new LambdaQueryWrapper<>();
        signinUserDataLambdaQueryWrapper.in(!uids.isEmpty(),SigninUserData::getUserId,uids);
        Map<Long, SigninUserData> signinUserDataMap = signinUserDataDao.list(signinUserDataLambdaQueryWrapper).stream().collect(Collectors.toMap(SigninUserData::getUserId, Function.identity()));
        for (Object user : pageInfo.getRecords()) {
            User user1 = (User) user;
            //Todo:需要优化，将部门整个进缓存，在查询不到或者更改时更新单个缓存
            SysDept sysDept = iSysDeptService.getById(user1.getDeptId());
            //从部门继承的不能直接显示，或者需要却别开，不然很乱

            SigninDataResp signinDataResp = new SigninDataResp();


            String avatar = user1.getAvatar();
            if (StringUtils.isNotEmpty(avatar)) {
                avatar = OssDBUtil.toUseUrl(avatar);
            } else {
                avatar = "";
            }
            BeanUtils.copyProperties(user1,signinDataResp);
            signinDataResp.setId(String.valueOf(user1.getId()));
            signinDataResp.setDeptId(String.valueOf(sysDept.getId()));
            signinDataResp.setDeptName(sysDept.getDeptNameAll());
            SigninUserData signinUserData = signinUserDataMap.get(user1.getId());
            if (signinUserData==null){
                signinDataResp.setExistFace(false);
                signinDataResp.setExistCard(false);
                signinDataResp.setCardId("");
                signinDataResp.setUpdateTime(null);
                results.add(signinDataResp);
                continue;
            }
            signinDataResp.setExistFace(StringUtils.isNotEmpty(signinUserData.getFaceData()));
            signinDataResp.setExistCard(StringUtils.isNotEmpty(signinUserData.getCardId()));
            signinDataResp.setCardId(signinUserData.getCardId());
            signinDataResp.setUpdateTime(signinUserData.getUpdateTime());
            results.add(signinDataResp);
        }
        pageData.setPages(pageInfo.getPages());
        pageData.setTotal(pageInfo.getTotal());
        pageData.setCountId(pageInfo.getCountId());
        pageData.setCurrent(pageInfo.getCurrent());
        pageData.setSize(pageInfo.getSize());
        pageData.setRecords(results);
        pageData.setMaxLimit(pageInfo.getMaxLimit());
        return pageData;
    }

    @Override
    public List<SigninUserDataExcelDto> exportAllData() {
        List<SigninUserDataExcelDto> signinUserDataExcelDtos = new ArrayList<>();

        LambdaQueryWrapper<User> userLambdaQueryWrapper = new LambdaQueryWrapper<>();
        userLambdaQueryWrapper.select(User::getId,User::getDeptId,User::getName);
        List<User> list = userDao.list(userLambdaQueryWrapper);

        Set<Long> uids = list.stream().map(User::getId).collect(Collectors.toSet());
        LambdaQueryWrapper<SigninUserData> signinUserDataLambdaQueryWrapper = new LambdaQueryWrapper<>();
        signinUserDataLambdaQueryWrapper.in(!uids.isEmpty(),SigninUserData::getUserId,uids);
        Map<Long, SigninUserData> signinUserDataMap = signinUserDataDao.list(signinUserDataLambdaQueryWrapper).stream().collect(Collectors.toMap(SigninUserData::getUserId, Function.identity()));
        for (User user : list) {
            SigninUserDataExcelDto signinUserDataExcelDto = new SigninUserDataExcelDto();
            SigninUserData signinUserData = signinUserDataMap.get(user.getId());
            if (signinUserData!=null){
                BeanUtils.copyProperties(signinUserData,signinUserDataExcelDto);

            }
            signinUserDataExcelDto.setUserId(user.getId());

            signinUserDataExcelDto.setName(user.getName());
            SysDept byId = iSysDeptService.getById(user.getDeptId());
            if (byId==null){
                continue;
            }
            signinUserDataExcelDto.setDeptName(byId.getDeptNameAll());
            signinUserDataExcelDtos.add(signinUserDataExcelDto);
        }
        return signinUserDataExcelDtos;
    }

    @Transactional
    @Override
    public String importSigninUserCardData(List<SigninUserDataExcelDto> dataList, boolean updateSupport) {
        List<UserDataImportErrorDto> errorData = new ArrayList<>();
        for (SigninUserDataExcelDto signinUserDataExcelDto : dataList) {
            if(signinUserDataExcelDto==null){
                continue;
            }
            if (signinUserDataExcelDto.getUserId()==null){
                continue;
            }
            User byId = userDao.getById(signinUserDataExcelDto.getUserId());
            if (byId==null){
                UserDataImportErrorDto signinUserDataExcelDto1 = new UserDataImportErrorDto();
                signinUserDataExcelDto1.setUserId(signinUserDataExcelDto.getUserId());
                signinUserDataExcelDto1.setError("用户不存在!");
                errorData.add(signinUserDataExcelDto1);
                continue;
            }
            LambdaQueryWrapper<SigninUserData> signinUserDataLambdaQueryWrapper = new LambdaQueryWrapper<>();
            signinUserDataLambdaQueryWrapper.eq(SigninUserData::getUserId,byId.getId());
            SigninUserData one = signinUserDataDao.getOne(signinUserDataLambdaQueryWrapper);


            if (one!=null){
                one.setUpdateTime(LocalDateTime.now());
                if (!updateSupport){
                    if (StringUtils.isEmpty(one.getCardId())){
                        one.setCardId(signinUserDataExcelDto.getCardId());
                    }
                }else {
                    one.setCardId(signinUserDataExcelDto.getCardId());
                }
                signinUserDataDao.updateById(one);
                continue;
            }
            SigninUserData signinUserData = new SigninUserData();
            signinUserData.setUpdateTime(LocalDateTime.now());

            signinUserData.setUserId(byId.getId());
            signinUserData.setCardId(signinUserDataExcelDto.getCardId());
            signinUserDataDao.save(signinUserData);
        }
        if (errorData.size()!=0){
            ExcelUtil<UserDataImportErrorDto> util = new ExcelUtil<UserDataImportErrorDto>(UserDataImportErrorDto.class,minIoProperties.getBucketName());
            return util.exportExcel(errorData, "失败数据").getData();
        }
        return "";// 这样就是操作成功，不能返回任何东西
    }

    @Transactional
    @Override
    public String downloadSigninFaceData(SigninUserFaceDataReq signinUserFaceDataReq) {
        SigninDeviceDto signinDevice = this.checkDeviceStatus(signinUserFaceDataReq.getDeviceId(), "face");

        if (!signinDevice.getOnline()) {
            throw new CustomException("设备掉线，同步失败");
        }
        // 设置请求头
        HttpHeaders headers = new HttpHeaders();
        headers.set("secert_h", signinDevice.getSecret());

        // 创建 HttpEntity，并传入请求头
        HttpEntity<String> entity = new HttpEntity<>(headers);
        String url = "http://" + signinDevice.getAddress() + ":" + signinDevice.getPort() + "/list_all_face";
        ResponseEntity<List<SigninUserFaceDataDto>> exchange = restTemplate.exchange(url, HttpMethod.GET, entity, new ParameterizedTypeReference<List<SigninUserFaceDataDto>>() {
        });
        List<SigninUserFaceDataDto> body = exchange.getBody();
        Map<String, SigninUserFaceDataDto> stringSigninUserFaceDataDtoMap = body.stream()
                .collect(Collectors.toMap(SigninUserFaceDataDto::getStudentId, item -> item));

        List<SigninUserFaceDataResp> data = signinUserFaceDataReq.getData();
        for (SigninUserFaceDataResp datum : data) {
            User byId = userDao.getById(datum.getUserId());
            if (byId == null) {
                throw new CustomException(datum.getUsername() + "用户不存在");
            }
            LambdaQueryWrapper<SigninUserData> objectLambdaQueryWrapper = new LambdaQueryWrapper<>();
            objectLambdaQueryWrapper.eq(SigninUserData::getUserId, byId.getId());
            SigninUserData one = signinUserDataDao.getOne(objectLambdaQueryWrapper);
            if (datum.isDeviceExist()) {
                // 更新本地
                if (one == null) {
                    //本地尚不存在，新建!
                    one = new SigninUserData();
                    one.setUserId(byId.getId());
                    one.setUpdateTime(LocalDateTime.now());
                    one.setFaceData(stringSigninUserFaceDataDtoMap.get(byId.getStudentId()).getFaceData());
                    signinUserDataDao.save(one);
                } else {
                    one.setFaceData(stringSigninUserFaceDataDtoMap.get(byId.getStudentId()).getFaceData());
                    signinUserDataDao.updateById(one);
                }

            } else {
                if (one != null) {
                    //本地存在远端不存在，删除
                    one.setFaceData(null);
                    signinUserDataDao.updateById(one);
                }
            }
        }
        return "同步成功";
    }

    @Override
    public List<SigninUserCardDataResp> getSigninCardData(String deviceId) {
        SigninDeviceDto signinDevice = this.checkDeviceStatus(deviceId, "card");
        // 设置请求头
        HttpHeaders headers = new HttpHeaders();
        headers.set("secert_h", signinDevice.getSecret());

        // 创建 HttpEntity，并传入请求头
        HttpEntity<String> entity = new HttpEntity<>(headers);
        String url = "http://" + signinDevice.getAddress() + ":" + signinDevice.getPort() + "/list_all_card";
        ResponseEntity<List<SigninUserCardDataDto>> exchange = restTemplate.exchange(url, HttpMethod.GET, entity, new ParameterizedTypeReference<List<SigninUserCardDataDto>>() {
        });
        List<SigninUserCardDataDto> body = exchange.getBody();
        List<User> userList = userDao.list();
        List<SigninUserCardDataResp> signinUserCardDataResps = new ArrayList<>();
        log.info("device:{},face{}", deviceId, body);
        // 将 List<Item> 转换为 Map<Integer, Item>

        Map<String, SigninUserCardDataDto> stringSigninUserCardDataDtoMap = body.stream()
                .filter(signinUserCardDataDto -> signinUserCardDataDto.getStudentId()!=null&&!signinUserCardDataDto.getStudentId().equals("")&&!signinUserCardDataDto.getStudentId().equals("null"))
                .collect(Collectors.toMap(SigninUserCardDataDto::getStudentId, item -> item));
        for (User user1 : userList) {
            SigninUserCardDataResp signinUserCardDataResp = new SigninUserCardDataResp();
            SysDept sysDept = iSysDeptService.getById(user1.getDeptId());
            //从部门继承的不能直接显示，或者需要却别开，不然很乱
            LambdaQueryWrapper<SigninUserData> signinUserDataLambdaQueryWrapper = new LambdaQueryWrapper<>();
            signinUserDataLambdaQueryWrapper.eq(SigninUserData::getUserId, user1.getId());
            SigninUserData one = signinUserDataDao.getOne(signinUserDataLambdaQueryWrapper);
            signinUserCardDataResp.setLocalExist(true);
            if (one == null || StringUtils.isEmpty(one.getCardId())) {
                // cardId里面真的有数据就是存在,不存在都设置为NULL
                signinUserCardDataResp.setLocalExist(false);
            }
            signinUserCardDataResp.setUserId(user1.getId());
            signinUserCardDataResp.setName(user1.getName());
            signinUserCardDataResp.setUsername(user1.getUsername());
            signinUserCardDataResp.setDeptId(user1.getDeptId());
            signinUserCardDataResp.setDeptName(sysDept.getDeptNameAll());
            signinUserCardDataResp.setStudentId(user1.getStudentId());
            signinUserCardDataResp.setDeviceExist(false);
            if (stringSigninUserCardDataDtoMap.get(user1.getStudentId()) != null) {
                signinUserCardDataResp.setDeviceExist(true);
            }
            signinUserCardDataResps.add(signinUserCardDataResp);
        }
        return signinUserCardDataResps;
    }

    @Transactional
    @Override
    public String uploadSigninCardData(SigninUserCardDataReq signinUserCardDataReq) {
        SigninDeviceDto signinDevice = this.checkDeviceStatus(signinUserCardDataReq.getDeviceId(), "card");

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
        List<SigninUserCardDataResp> data = signinUserCardDataReq.getData();
        for (SigninUserCardDataResp datum : data) {
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
                everyP.put("card_id", one.getCardId());
                everyP.put("is_none", false);

            } else {
                everyP.put("is_none", true);
            }
            datas.add(everyP);

        }
        requestBody.put("model", signinUserCardDataReq.getSyncModel());
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
    public String downloadSigninCardData(SigninUserCardDataReq signinUserCardDataReq) {
        SigninDeviceDto signinDevice = this.checkDeviceStatus(signinUserCardDataReq.getDeviceId(), "card");

        if (!signinDevice.getOnline()) {
            throw new CustomException("设备掉线，同步失败");
        }
        // 设置请求头
        HttpHeaders headers = new HttpHeaders();
        headers.set("secert_h", signinDevice.getSecret());

        // 创建 HttpEntity，并传入请求头
        HttpEntity<String> entity = new HttpEntity<>(headers);
        String url = "http://" + signinDevice.getAddress() + ":" + signinDevice.getPort() + "/list_all_card";
        ResponseEntity<List<SigninUserCardDataDto>> exchange = restTemplate.exchange(url, HttpMethod.GET, entity, new ParameterizedTypeReference<List<SigninUserCardDataDto>>() {
        });
        List<SigninUserCardDataDto> body = exchange.getBody();
        Map<String, SigninUserCardDataDto> stringSigninUserCardDataDtoMap = body.stream()
                .collect(Collectors.toMap(SigninUserCardDataDto::getStudentId, item -> item));

        List<SigninUserCardDataResp> data = signinUserCardDataReq.getData();
        for (SigninUserCardDataResp datum : data) {
            User byId = userDao.getById(datum.getUserId());
            if (byId == null) {
                throw new CustomException(datum.getUsername() + "用户不存在");
            }
            LambdaQueryWrapper<SigninUserData> objectLambdaQueryWrapper = new LambdaQueryWrapper<>();
            objectLambdaQueryWrapper.eq(SigninUserData::getUserId, byId.getId());
            SigninUserData one = signinUserDataDao.getOne(objectLambdaQueryWrapper);
            if (datum.isDeviceExist()) {
                // 更新本地
                if (one == null) {
                    //本地尚不存在，新建!
                    one = new SigninUserData();
                    one.setUserId(byId.getId());
                    one.setUpdateTime(LocalDateTime.now());
                    one.setCardId(stringSigninUserCardDataDtoMap.get(byId.getStudentId()).getCardId());
                    signinUserDataDao.save(one);
                } else {
                    one.setCardId(stringSigninUserCardDataDtoMap.get(byId.getStudentId()).getCardId());
                    signinUserDataDao.updateById(one);
                }

            } else {
                if (one != null) {
                    //本地存在远端不存在，删除
                    one.setCardId(null);
                    signinUserDataDao.updateById(one);
                }
            }
        }
        return "同步成功";
    }
}
