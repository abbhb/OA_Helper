package com.qc.printers.common.print.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ecwid.consul.v1.health.model.HealthService;
import com.qc.printers.common.common.CustomException;
import com.qc.printers.common.common.annotation.RedissonLock;
import com.qc.printers.common.common.domain.entity.PageData;
import com.qc.printers.common.common.service.ConsulService;
import com.qc.printers.common.common.utils.StringUtils;
import com.qc.printers.common.common.utils.ThreadLocalUtil;
import com.qc.printers.common.print.dao.SysPrintDeviceDao;
import com.qc.printers.common.print.dao.SysPrintDeviceUserDao;
import com.qc.printers.common.print.domain.dto.PrintDeviceUserDto;
import com.qc.printers.common.print.domain.entity.SysPrintDevice;
import com.qc.printers.common.print.domain.entity.SysPrintDeviceUser;
import com.qc.printers.common.print.domain.vo.PrintDeviceNotRegisterVO;
import com.qc.printers.common.print.domain.vo.request.CreatePrintDeviceReq;
import com.qc.printers.common.print.domain.vo.request.PrintDeviceUserQuery;
import com.qc.printers.common.print.domain.vo.request.PrintDeviceUserReq;
import com.qc.printers.common.print.domain.vo.request.UpdatePrintDeviceStatusReq;
import com.qc.printers.common.print.domain.vo.response.PrintDeviceVO;
import com.qc.printers.common.print.service.PrintDeviceManagerService;
import com.qc.printers.common.user.dao.UserDao;
import com.qc.printers.common.user.domain.dto.DeptManger;
import com.qc.printers.common.user.domain.dto.UserInfo;
import com.qc.printers.common.user.domain.entity.SysDept;
import com.qc.printers.common.user.domain.entity.User;
import com.qc.printers.common.user.service.ISysDeptService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserCache;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
public class PrintDeviceManagerServiceImpl implements PrintDeviceManagerService {
    @Autowired
    private SysPrintDeviceDao sysPrintDeviceDao;

    @Autowired
    private SysPrintDeviceUserDao sysPrintDeviceUserDao;

    @Autowired
    private ConsulService consulService;

    @Autowired
    private UserDao userDao;
    @Autowired
    private ISysDeptService iSysDeptService;




    @Override
    public List<PrintDeviceNotRegisterVO> getUnRegisterPrintDeviceList() {
        List<HealthService> registeredServices = consulService.getPrintDeviceServices();
        List<PrintDeviceNotRegisterVO> printDeviceNotRegisterVOs = new ArrayList<>();
        for (HealthService registeredService : registeredServices) {
            PrintDeviceNotRegisterVO printDeviceNotRegisterVO = new PrintDeviceNotRegisterVO();
            printDeviceNotRegisterVO.setDescription(registeredService.getService().getMeta().get("ZName"));
            printDeviceNotRegisterVO.setName(printDeviceNotRegisterVO.getDescription());
//            printDeviceNotRegisterVO.set(registeredService.getService().getMeta().get("ZSecret"));
            //筛选了只要状态正常的，所以这里全是正常的
            printDeviceNotRegisterVO.setStatus(1);
            printDeviceNotRegisterVO.setId(registeredService.getService().getId());
            printDeviceNotRegisterVOs.add(printDeviceNotRegisterVO);
        }
        List<String> list = printDeviceNotRegisterVOs.stream().map(PrintDeviceNotRegisterVO::getId).toList();
        LambdaQueryWrapper<SysPrintDevice> sysPrintDeviceLambdaQueryWrapper = new LambdaQueryWrapper<>();
        if (list.size()>1){
            sysPrintDeviceLambdaQueryWrapper.in(SysPrintDevice::getDeviceId,list);
        }else if (list.size()==1){
            sysPrintDeviceLambdaQueryWrapper.eq(SysPrintDevice::getDeviceId,list.get(0));
        }
        List<SysPrintDevice> existSysPrintDevices = sysPrintDeviceDao.list(sysPrintDeviceLambdaQueryWrapper);
        Map<String, SysPrintDevice> collect = existSysPrintDevices.stream().collect(Collectors.toMap(SysPrintDevice::getDeviceId, Function.identity()));
        List<PrintDeviceNotRegisterVO> printDeviceNotRegisterVOS = new ArrayList<>();
        for (PrintDeviceNotRegisterVO printDeviceNotRegisterVO : printDeviceNotRegisterVOs) {
            if (StringUtils.isEmpty(printDeviceNotRegisterVO.getId())){
                // 异常的设备，id不能为空
                continue;
            }
            if (collect.containsKey(printDeviceNotRegisterVO.getId())) {
                // 已存在
                continue;
            }
            printDeviceNotRegisterVOS.add(printDeviceNotRegisterVO);
        }
        return printDeviceNotRegisterVOS;
    }

    @Transactional
    @Override
    public String createPrintDevice(CreatePrintDeviceReq data) {
        if (StringUtils.isEmpty(data.getDeviceId())){
            throw new CustomException("设备id必填");
        }
        if (StringUtils.isEmpty(data.getDeviceSecret())){
            throw new CustomException("设备密钥必填");
        }
        // 校验设备是否存在与在线
        List<HealthService> registeredServices = consulService.getPrintDeviceServices();
        Optional<HealthService> first = registeredServices.stream().filter(obj -> obj.getService().getId().equals(data.getDeviceId())).findFirst();
        if (first.isEmpty()) {
            log.error("设备不在线:%s".formatted(data.getDeviceId()));
            throw new CustomException("设备不在线");
        }
        String secret = first.get().getService().getMeta().get("ZSecret");
        if (StringUtils.isEmpty(secret)){
            throw new CustomException("设备不兼容，为了安全性，请先注册密钥！");
        }
        // 校验密钥是否正确
        if (!secret.equals(data.getDeviceSecret())){
            throw new CustomException("密钥不正确，请检查密钥");
        }


        SysPrintDevice sysPrintDevice = new SysPrintDevice();
        sysPrintDevice.setDeviceDescription(first.get().getService().getMeta().get("ZName"));
        sysPrintDevice.setDeviceName(sysPrintDevice.getDeviceDescription());
        sysPrintDevice.setDeviceId(first.get().getService().getId());
        sysPrintDevice.setStatus(1);
        sysPrintDeviceDao.save(sysPrintDevice);
        // 注册后为owner
        UserInfo currentUser = ThreadLocalUtil.getCurrentUser();
        SysPrintDeviceUser build = SysPrintDeviceUser.builder()
                .linkId(currentUser.getId())
                .linkType(1)
                .printDeviceId(sysPrintDevice.getId())
                .role(1)
                .build();
        sysPrintDeviceUserDao.save(build);
        return "注册打印机成功";
    }

    @Transactional
    @Override
    public String deletePrintDevice(String id) {
        if (StringUtils.isEmpty(id)){
            throw new CustomException("请指定删除对象");
        }
        SysPrintDevice byId = sysPrintDeviceDao.getById(Long.valueOf(id));
        if (byId==null) {
            throw new CustomException("过时的信息，请先刷新");
        }
        UserInfo currentUser = ThreadLocalUtil.getCurrentUser();
        sysPrintDeviceDao.removeById(byId.getId());
        LambdaQueryWrapper<SysPrintDeviceUser> sysPrintDeviceUserLambdaQueryWrapper = new LambdaQueryWrapper<>();
        sysPrintDeviceUserLambdaQueryWrapper.eq(SysPrintDeviceUser::getPrintDeviceId,byId.getId());
        sysPrintDeviceUserDao.remove(sysPrintDeviceUserLambdaQueryWrapper);
        return "删除成功";
    }

    @Transactional
    @Override
    public String updatePrintDeviceStatus(UpdatePrintDeviceStatusReq data) {
        if (StringUtils.isEmpty(data.getId())){
            throw new CustomException("请指定对象");
        }
        if (data.getStatus()==null){
            throw new CustomException("状态值异常");
        }
        switch (data.getStatus()){
            case 1:
            case 0:
                break;
            default:
                throw new CustomException("不被支持的状态");
        }
        SysPrintDevice byId = sysPrintDeviceDao.getById(Long.valueOf(data.getId()));
        if (byId==null){
            throw new CustomException("过时的信息，请先刷新");
        }
        byId.setStatus(data.getStatus());
        sysPrintDeviceDao.updateById(byId);
        return "切换状态成功";
    }

    @Override
    public PageData<PrintDeviceUserDto> getPrintDeviceUsers(PrintDeviceUserQuery params) {
        if (params.getPrintDeviceId()==null){
            throw new CustomException("设备ID为空");
        }
        if (params.getRole()==null){
            params.setRole(0);
        }
        Page<SysPrintDeviceUser> pageInfo = new Page<>(params.getPageNum(), params.getPageSize());
        PageData<PrintDeviceUserDto> pageData = new PageData<>();
        LambdaQueryWrapper<SysPrintDeviceUser> sysPrintDeviceUserLambdaQueryWrapper = new LambdaQueryWrapper<>();
        sysPrintDeviceUserLambdaQueryWrapper.eq(SysPrintDeviceUser::getPrintDeviceId,params.getPrintDeviceId());
        sysPrintDeviceUserLambdaQueryWrapper.orderByDesc(SysPrintDeviceUser::getLinkType);

        if (params.getRole().equals(0)){
            sysPrintDeviceUserLambdaQueryWrapper.orderByAsc(SysPrintDeviceUser::getRole);
        }else {
            sysPrintDeviceUserLambdaQueryWrapper.eq(SysPrintDeviceUser::getRole,params.getRole());
        }
        sysPrintDeviceUserDao.page(pageInfo,sysPrintDeviceUserLambdaQueryWrapper);
        List<PrintDeviceUserDto> results = new ArrayList<>();
        for (SysPrintDeviceUser sysPrintDeviceUser : pageInfo.getRecords()) {
            PrintDeviceUserDto printDeviceUserDto = new PrintDeviceUserDto();
            printDeviceUserDto.setPrintDeviceId(String.valueOf(sysPrintDeviceUser.getPrintDeviceId()));
            printDeviceUserDto.setUserId(String.valueOf(sysPrintDeviceUser.getLinkId()));
            printDeviceUserDto.setRole(sysPrintDeviceUser.getRole());
            printDeviceUserDto.setId(sysPrintDeviceUser.getId());
            if (sysPrintDeviceUser.getLinkType().equals(1)){
                User byId = userDao.getById(sysPrintDeviceUser.getLinkId());
                if (byId==null){
                    continue;
                }
                printDeviceUserDto.setUsername(byId.getName());
            }else if (sysPrintDeviceUser.getLinkType().equals(2)){
                SysDept sysDept = iSysDeptService.getById(sysPrintDeviceUser.getLinkId());
                if (sysDept==null){
                    continue;
                }
                printDeviceUserDto.setUsername("[用户组]"+sysDept.getDeptNameAll());
            }

            results.add(printDeviceUserDto);
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
    public List<Long> getPrintDeviceUserIds(Long printDeviceId) {
        if (printDeviceId==null){
            throw new CustomException("请传入设备ID");
        }
        LambdaQueryWrapper<SysPrintDeviceUser> userLambdaQueryWrapper = new LambdaQueryWrapper<>();
        userLambdaQueryWrapper.eq(SysPrintDeviceUser::getPrintDeviceId,printDeviceId);
        userLambdaQueryWrapper.eq(SysPrintDeviceUser::getLinkType,1);
        userLambdaQueryWrapper.select(SysPrintDeviceUser::getLinkId);

        List<SysPrintDeviceUser> list = sysPrintDeviceUserDao.list(userLambdaQueryWrapper);
        return list.stream().map(SysPrintDeviceUser::getLinkId).toList();
    }

    /**
     * 添加打印机相关联用户
     * @param data
     * @return
     */
    @Transactional
    @RedissonLock(prefixKey = "print_device",key = "#data.printDeviceId")
    @Override
    public String addPrintDeviceUsers(PrintDeviceUserReq data) {
        if (data.getPrintDeviceId()==null){
            throw new CustomException("设备ID不能为空");
        }
        if (data.getRole()==null){
            data.setRole(3);// 默认添加的用户就是用户
        }
        if (data.getUserIds()==null|| data.getUserIds().isEmpty()){
            throw new CustomException("必须包含要添加的用户");
        }
        // 校验设备是否注册
        SysPrintDevice sysPrintDevice = sysPrintDeviceDao.getById(Long.valueOf(data.getPrintDeviceId()));
        if (sysPrintDevice==null){
            throw new CustomException("设备不存在，是否被人删除了");
        }
        Set<Long> ids = new HashSet<>();
        for (String userId : data.getUserIds()) {
            User byId = userDao.getById(Long.valueOf(userId));
            if (byId==null){
                throw new CustomException("选中的用户id不存在数据:"+userId);
            }
            ids.add(byId.getId());
        }
        List<Long> userIdList = ids.stream().toList();
        List<Long> userIdResultList = new ArrayList<>();
        for (Long l : userIdList) {
            LambdaQueryWrapper<SysPrintDeviceUser> NotFoundLambdaQueryWrapper = new LambdaQueryWrapper<>();
            NotFoundLambdaQueryWrapper.eq(SysPrintDeviceUser::getPrintDeviceId,Long.valueOf(data.getPrintDeviceId()));
            NotFoundLambdaQueryWrapper.eq(SysPrintDeviceUser::getLinkId,l);
            NotFoundLambdaQueryWrapper.eq(SysPrintDeviceUser::getLinkType,1);

            int count = sysPrintDeviceUserDao.count(NotFoundLambdaQueryWrapper);
            if (count>0){
                continue;
            }
            userIdResultList.add(l);
        }


        for (Long userId : userIdResultList) {
            SysPrintDeviceUser build = SysPrintDeviceUser.builder()
                    .linkType(1)
                    .linkId(userId)
                    .printDeviceId(Long.valueOf(data.getPrintDeviceId()))
                    .role(data.getRole())
                    .build();
            sysPrintDeviceUserDao.save(build);
        }
        return "添加用户成功";
    }

    @Transactional
    @RedissonLock(prefixKey = "print_device",key = "#printDeviceId")
    @Override
    public String removePrintDeviceUser(String printDeviceId, String userId) {
        // 校验设备是否注册
        SysPrintDevice sysPrintDevice = sysPrintDeviceDao.getById(Long.valueOf(printDeviceId));
        if (sysPrintDevice==null){
            throw new CustomException("设备不存在，是否被人删除了");
        }
        String[] split = userId.split(",");
        List<Long> userIds = new ArrayList<>();
        for (String s : split) {
            if (StringUtils.isEmpty(StringUtils.trim(s))){
                continue;
            }
            userIds.add(Long.valueOf(StringUtils.trim(s)));
        }
        if (userIds.isEmpty()){
            throw new CustomException("请传入用户");
        }
        UserInfo currentUser = ThreadLocalUtil.getCurrentUser();
        for (Long id : userIds) {
            if (id.equals(currentUser.getId())){
                throw new CustomException("请勿操作自己");
            }
        }
        LambdaQueryWrapper<SysPrintDeviceUser> operatorLambdaQueryWrapper = new LambdaQueryWrapper<>();
        operatorLambdaQueryWrapper.eq(SysPrintDeviceUser::getPrintDeviceId,sysPrintDevice.getId());
        operatorLambdaQueryWrapper.eq(SysPrintDeviceUser::getLinkId,currentUser.getId());
        operatorLambdaQueryWrapper.eq(SysPrintDeviceUser::getLinkType,1);
        // 获取当前用户在该设备的对象
        SysPrintDeviceUser operator = sysPrintDeviceUserDao.getOne(operatorLambdaQueryWrapper);
        // 构造批量删除绑定关系
        LambdaQueryWrapper<SysPrintDeviceUser> sysPrintDeviceUserLambdaQueryWrapper = new LambdaQueryWrapper<>();
        sysPrintDeviceUserLambdaQueryWrapper.eq(SysPrintDeviceUser::getPrintDeviceId,Long.valueOf(printDeviceId));
        sysPrintDeviceUserLambdaQueryWrapper.eq(SysPrintDeviceUser::getLinkType,1);
        sysPrintDeviceUserLambdaQueryWrapper.gt(SysPrintDeviceUser::getRole,operator.getRole());
        if (userIds.size()>1){
            sysPrintDeviceUserLambdaQueryWrapper.in(SysPrintDeviceUser::getLinkId,userIds);
        }else {
            sysPrintDeviceUserLambdaQueryWrapper.eq(SysPrintDeviceUser::getLinkId,userIds.get(0));
        }
        // 删除
        sysPrintDeviceUserDao.remove(sysPrintDeviceUserLambdaQueryWrapper);
        return "删除用户成功";
    }

    @Transactional
    @Override
    public String updatePrintDeviceUserRole(PrintDeviceUserReq data) {
        if (StringUtils.isEmpty(data.getPrintDeviceId())){
            throw new CustomException("请填写设备id");
        }
        if (data.getUserIds()==null){
            throw new CustomException("请指定操作对象");
        }
        if (data.getRole()==null){
            throw new CustomException("请设置角色");
        }
        if (data.getUserIds().isEmpty()){
            throw new CustomException("请保证操作对象");
        }
        SysPrintDevice byId = sysPrintDeviceDao.getById(data.getPrintDeviceId());
        if (byId==null){
            throw new CustomException("未注册设备ID");
        }
        if (data.getRole().equals(1)){
            // 等效转交所有权
            // 有且一人
            if (data.getUserIds().size()>1){
                throw new CustomException("转交所有权只能选择一人");
            }
        }
        List<Long> userIds = new ArrayList<>();
        for (String id : data.getUserIds()) {
            userIds.add(Long.valueOf(StringUtils.trim(id)));
        }
        UserInfo currentUser = ThreadLocalUtil.getCurrentUser();
        for (Long id : userIds) {
            if (id.equals(currentUser.getId())){
                throw new CustomException("请勿操作自己");
            }
        }
        LambdaUpdateWrapper<SysPrintDeviceUser> sysPrintDeviceUserLambdaQueryWrapper = new LambdaUpdateWrapper<>();
        sysPrintDeviceUserLambdaQueryWrapper.eq(SysPrintDeviceUser::getPrintDeviceId,byId.getId());
        sysPrintDeviceUserLambdaQueryWrapper.eq(SysPrintDeviceUser::getLinkType,1);
        if (data.getUserIds().size()>1){
            sysPrintDeviceUserLambdaQueryWrapper.in(SysPrintDeviceUser::getLinkId,userIds);
        }else {
            sysPrintDeviceUserLambdaQueryWrapper.eq(SysPrintDeviceUser::getLinkId,userIds.get(0));
        }
        sysPrintDeviceUserLambdaQueryWrapper.set(SysPrintDeviceUser::getRole,data.getRole());
        sysPrintDeviceUserDao.update(sysPrintDeviceUserLambdaQueryWrapper);
        if (data.getRole().equals(1)){
            // 将自己的所有者更新成user
            LambdaUpdateWrapper<SysPrintDeviceUser> selfUpdate = new LambdaUpdateWrapper<>();
            selfUpdate.eq(SysPrintDeviceUser::getLinkId,currentUser.getId());
            selfUpdate.eq(SysPrintDeviceUser::getLinkType,1);
            selfUpdate.eq(SysPrintDeviceUser::getPrintDeviceId,byId.getId());
            selfUpdate.set(SysPrintDeviceUser::getRole,3);
            sysPrintDeviceUserDao.update(selfUpdate);
        }
        return "更新成功";
    }


    @Override
    public List<PrintDeviceVO> getPrintDeviceList() {
        UserInfo currentUser = ThreadLocalUtil.getCurrentUser();
        LambdaQueryWrapper<SysPrintDeviceUser> sysPrintDeviceUserLambdaQueryWrapper = new LambdaQueryWrapper<>();
        sysPrintDeviceUserLambdaQueryWrapper.eq(SysPrintDeviceUser::getLinkId,currentUser.getId());
        sysPrintDeviceUserLambdaQueryWrapper.eq(SysPrintDeviceUser::getLinkType,1);
        List<SysPrintDeviceUser> list = sysPrintDeviceUserDao.list(sysPrintDeviceUserLambdaQueryWrapper);
        List<PrintDeviceVO> printDeviceVOS = new ArrayList<>();

        for (SysPrintDeviceUser sysPrintDeviceUser : list) {
            Long printDeviceId = sysPrintDeviceUser.getPrintDeviceId();
            SysPrintDevice sysPrintDevice = sysPrintDeviceDao.getById(printDeviceId);
            if (sysPrintDevice==null){
                continue;
                // 可能设备已删除
            }
            LambdaQueryWrapper<SysPrintDeviceUser> ownerSDUL = new LambdaQueryWrapper<>();
            ownerSDUL.eq(SysPrintDeviceUser::getRole,1);
            ownerSDUL.eq(SysPrintDeviceUser::getLinkType,1);
            ownerSDUL.eq(SysPrintDeviceUser::getPrintDeviceId,sysPrintDevice.getId());
            SysPrintDeviceUser ownerSDU = sysPrintDeviceUserDao.getOne(ownerSDUL);
            if (ownerSDU==null){
                // owner 不存在
                continue;
            }
            PrintDeviceVO printDeviceVO = new PrintDeviceVO();
            printDeviceVO.setDeviceDescription(sysPrintDevice.getDeviceDescription());
            printDeviceVO.setDeviceName(sysPrintDevice.getDeviceName());
            printDeviceVO.setDeviceId(sysPrintDevice.getDeviceId());
            printDeviceVO.setCreateTime(sysPrintDevice.getCreateTime());
            printDeviceVO.setCreateUserName(userDao.getById(sysPrintDevice.getCreateUser()).getName());
            printDeviceVO.setOwnerName(userDao.getById(ownerSDU.getLinkId()).getName());
            printDeviceVO.setStatus(sysPrintDevice.getStatus());
            printDeviceVO.setUserRole(sysPrintDeviceUser.getRole());
            printDeviceVO.setId(sysPrintDevice.getId());
            printDeviceVOS.add(printDeviceVO);
        }
        return printDeviceVOS;
    }


}
