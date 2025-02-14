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
import com.qc.printers.common.print.dao.SysPrintDeviceLinkDao;
import com.qc.printers.common.print.domain.dto.PrintDeviceLinkDto;
import com.qc.printers.common.print.domain.entity.SysPrintDevice;
import com.qc.printers.common.print.domain.entity.SysPrintDeviceLink;
import com.qc.printers.common.print.domain.vo.PrintDeviceNotRegisterVO;
import com.qc.printers.common.print.domain.vo.request.CreatePrintDeviceReq;
import com.qc.printers.common.print.domain.vo.request.PrintDeviceLinkQuery;
import com.qc.printers.common.print.domain.vo.request.PrintDeviceLinkReq;
import com.qc.printers.common.print.domain.vo.request.UpdatePrintDeviceStatusReq;
import com.qc.printers.common.print.domain.vo.response.PrintDeviceVO;
import com.qc.printers.common.print.service.PrintDeviceManagerService;
import com.qc.printers.common.user.dao.UserDao;
import com.qc.printers.common.user.domain.dto.UserInfo;
import com.qc.printers.common.user.domain.entity.SysDept;
import com.qc.printers.common.user.domain.entity.User;
import com.qc.printers.common.user.service.ISysDeptService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
public class PrintDeviceManagerServiceImpl implements PrintDeviceManagerService {
    @Autowired
    private SysPrintDeviceDao sysPrintDeviceDao;

    @Autowired
    private SysPrintDeviceLinkDao sysPrintDeviceLinkDao;

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
        SysPrintDeviceLink build = SysPrintDeviceLink.builder()
                .linkId(currentUser.getId())
                .linkType(1)
                .printDeviceId(sysPrintDevice.getId())
                .role(1)
                .build();
        sysPrintDeviceLinkDao.save(build);
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
        LambdaQueryWrapper<SysPrintDeviceLink> sysPrintDeviceUserLambdaQueryWrapper = new LambdaQueryWrapper<>();
        sysPrintDeviceUserLambdaQueryWrapper.eq(SysPrintDeviceLink::getPrintDeviceId,byId.getId());
        sysPrintDeviceLinkDao.remove(sysPrintDeviceUserLambdaQueryWrapper);
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
    public PageData<PrintDeviceLinkDto> getPrintDeviceLinks(PrintDeviceLinkQuery params) {
        if (StringUtils.isEmpty(params.getPrintDeviceId())){
            throw new CustomException("设备ID为空");
        }
        if (params.getRole()==null){
            params.setRole(0);
        }
        Page<SysPrintDeviceLink> pageInfo = new Page<>(params.getPageNum(), params.getPageSize());
        PageData<PrintDeviceLinkDto> pageData = new PageData<>();
        LambdaQueryWrapper<SysPrintDeviceLink> sysPrintDeviceUserLambdaQueryWrapper = new LambdaQueryWrapper<>();
        sysPrintDeviceUserLambdaQueryWrapper.eq(SysPrintDeviceLink::getPrintDeviceId,params.getPrintDeviceId());
        sysPrintDeviceUserLambdaQueryWrapper.orderByDesc(SysPrintDeviceLink::getLinkType);

        if (params.getRole().equals(0)){
            sysPrintDeviceUserLambdaQueryWrapper.orderByAsc(SysPrintDeviceLink::getRole);
        }else {
            sysPrintDeviceUserLambdaQueryWrapper.eq(SysPrintDeviceLink::getRole,params.getRole());
        }
        sysPrintDeviceLinkDao.page(pageInfo,sysPrintDeviceUserLambdaQueryWrapper);
        List<PrintDeviceLinkDto> results = new ArrayList<>();
        for (SysPrintDeviceLink sysPrintDeviceLink : pageInfo.getRecords()) {
            PrintDeviceLinkDto printDeviceLinkDto = new PrintDeviceLinkDto();
            printDeviceLinkDto.setPrintDeviceId(String.valueOf(sysPrintDeviceLink.getPrintDeviceId()));
            printDeviceLinkDto.setLinkId(String.valueOf(sysPrintDeviceLink.getLinkId()));
            printDeviceLinkDto.setRole(sysPrintDeviceLink.getRole());
            printDeviceLinkDto.setId(sysPrintDeviceLink.getId());
            // 1:user 2:dept
            printDeviceLinkDto.setLinkType(sysPrintDeviceLink.getLinkType());
            if (sysPrintDeviceLink.getLinkType().equals(1)){
                User byId = userDao.getById(sysPrintDeviceLink.getLinkId());
                if (byId==null){
                    continue;
                }
                printDeviceLinkDto.setLinkName(byId.getName());
            }else if (sysPrintDeviceLink.getLinkType().equals(2)){
                SysDept sysDept = iSysDeptService.getById(sysPrintDeviceLink.getLinkId());
                if (sysDept==null){
                    continue;
                }
                printDeviceLinkDto.setLinkName("[用户组]"+sysDept.getDeptNameAll());
            }

            results.add(printDeviceLinkDto);
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
    public Map<Integer,List<Long>> getPrintDeviceLinkIds(Long printDeviceId) {
        if (printDeviceId==null){
            throw new CustomException("请传入设备ID");
        }
        LambdaQueryWrapper<SysPrintDeviceLink> userLambdaQueryWrapper = new LambdaQueryWrapper<>();
        userLambdaQueryWrapper.eq(SysPrintDeviceLink::getPrintDeviceId,printDeviceId);
        userLambdaQueryWrapper.select(SysPrintDeviceLink::getLinkId,SysPrintDeviceLink::getLinkType);

        List<SysPrintDeviceLink> list = sysPrintDeviceLinkDao.list(userLambdaQueryWrapper);
        Map<Integer, List<Long>> listMap = new HashMap<>();
        listMap.put(1,list.stream().filter(sysPrintDeviceLink -> sysPrintDeviceLink.getLinkType().equals(1)).map(SysPrintDeviceLink::getLinkId).toList());
        listMap.put(2,list.stream().filter(sysPrintDeviceLink -> sysPrintDeviceLink.getLinkType().equals(2)).map(SysPrintDeviceLink::getLinkId).toList());

        return listMap;
    }

    /**
     * 添加打印机相关联用户
     * @param data
     * @return
     */
    @Transactional
    @RedissonLock(prefixKey = "print_device",key = "#data.printDeviceId")
    @Override
    public String addPrintDeviceLinks(PrintDeviceLinkReq data) {
        if (data.getPrintDeviceId()==null){
            throw new CustomException("设备ID不能为空");
        }
        if (data.getRole()==null){
            data.setRole(3);// 默认添加的用户就是用户
        }
        if (data.getLinkIds()==null|| data.getLinkIds().isEmpty()){
            throw new CustomException("必须包含要添加的用户");
        }
        // 校验设备是否注册
        SysPrintDevice sysPrintDevice = sysPrintDeviceDao.getById(Long.valueOf(data.getPrintDeviceId()));
        if (sysPrintDevice==null){
            throw new CustomException("设备不存在，是否被人删除了");
        }
        Set<Long> ids = new HashSet<>();
        for (String linkId : data.getLinkIds()) {
            if (data.getLinkType().equals(1)){
                User byId = userDao.getById(Long.valueOf(linkId));
                if (byId==null){
                    throw new CustomException("选中的用户id不存在数据:"+linkId);
                }
                ids.add(byId.getId());
            }else if (data.getLinkType().equals(2)){
                SysDept byId = iSysDeptService.getById(Long.valueOf(linkId));
                if (byId==null){
                    throw new CustomException("选中的部门id不存在数据:"+linkId);
                }
                ids.add(byId.getId());
            }

        }
        List<Long> linkIdList = ids.stream().toList();
        List<Long> linkIdResultList = new ArrayList<>();
        for (Long l : linkIdList) {
            LambdaQueryWrapper<SysPrintDeviceLink> NotFoundLambdaQueryWrapper = new LambdaQueryWrapper<>();
            NotFoundLambdaQueryWrapper.eq(SysPrintDeviceLink::getPrintDeviceId,Long.valueOf(data.getPrintDeviceId()));
            NotFoundLambdaQueryWrapper.eq(SysPrintDeviceLink::getLinkId,l);
            NotFoundLambdaQueryWrapper.eq(SysPrintDeviceLink::getLinkType,data.getLinkType());

            int count = sysPrintDeviceLinkDao.count(NotFoundLambdaQueryWrapper);
            if (count>0){
                continue;
            }
            linkIdResultList.add(l);
        }


        for (Long linkId : linkIdResultList) {
            SysPrintDeviceLink build = SysPrintDeviceLink.builder()
                    .linkType(data.getLinkType())
                    .linkId(linkId)
                    .printDeviceId(Long.valueOf(data.getPrintDeviceId()))
                    .role(data.getRole())
                    .build();
            sysPrintDeviceLinkDao.save(build);
        }
        return "添加成功";
    }

    @Transactional
    @RedissonLock(prefixKey = "print_device",key = "#printDeviceId")
    @Override
    public String removePrintDeviceLink(String printDeviceId,String linkId,Integer linkType) {
        // 校验设备是否注册
        SysPrintDevice sysPrintDevice = sysPrintDeviceDao.getById(Long.valueOf(printDeviceId));
        if (sysPrintDevice==null){
            throw new CustomException("设备不存在，是否被人删除了");
        }
        String[] split = linkId.split(",");
        List<Long> linkIds = new ArrayList<>();
        for (String s : split) {
            if (StringUtils.isEmpty(StringUtils.trim(s))){
                continue;
            }
            linkIds.add(Long.valueOf(StringUtils.trim(s)));
        }
        if (linkIds.isEmpty()){
            throw new CustomException("请传入用户");
        }
        UserInfo currentUser = ThreadLocalUtil.getCurrentUser();
        for (Long id : linkIds) {
            if (id.equals(currentUser.getId())){
                throw new CustomException("请勿操作自己");
            }
        }
        LambdaQueryWrapper<SysPrintDeviceLink> operatorLambdaQueryWrapper = new LambdaQueryWrapper<>();
        operatorLambdaQueryWrapper.eq(SysPrintDeviceLink::getPrintDeviceId,sysPrintDevice.getId());
        operatorLambdaQueryWrapper.eq(SysPrintDeviceLink::getLinkId,currentUser.getId());
        operatorLambdaQueryWrapper.eq(SysPrintDeviceLink::getLinkType,1);
        // 获取当前用户在该设备的对象
        SysPrintDeviceLink operator = sysPrintDeviceLinkDao.getOne(operatorLambdaQueryWrapper);
        // 构造批量删除绑定关系
        LambdaQueryWrapper<SysPrintDeviceLink> sysPrintDeviceUserLambdaQueryWrapper = new LambdaQueryWrapper<>();
        sysPrintDeviceUserLambdaQueryWrapper.eq(SysPrintDeviceLink::getPrintDeviceId,Long.valueOf(printDeviceId));
        sysPrintDeviceUserLambdaQueryWrapper.eq(SysPrintDeviceLink::getLinkType,linkType);
        sysPrintDeviceUserLambdaQueryWrapper.gt(SysPrintDeviceLink::getRole,operator.getRole());
        if (linkIds.size()>1){
            sysPrintDeviceUserLambdaQueryWrapper.in(SysPrintDeviceLink::getLinkId,linkIds);
        }else {
            sysPrintDeviceUserLambdaQueryWrapper.eq(SysPrintDeviceLink::getLinkId,linkIds.get(0));
        }
        // 删除
        sysPrintDeviceLinkDao.remove(sysPrintDeviceUserLambdaQueryWrapper);
        return "删除成功";
    }

    @Transactional
    @Override
    public String updatePrintDeviceLinkRole(PrintDeviceLinkReq data) {
        if (StringUtils.isEmpty(data.getPrintDeviceId())){
            throw new CustomException("请填写设备id");
        }
        if (data.getLinkIds()==null){
            throw new CustomException("请指定操作对象");
        }
        if (data.getRole()==null){
            throw new CustomException("请设置角色");
        }
        if (data.getLinkIds().isEmpty()){
            throw new CustomException("请保证操作对象");
        }
        SysPrintDevice byId = sysPrintDeviceDao.getById(data.getPrintDeviceId());
        if (byId==null){
            throw new CustomException("未注册设备ID");
        }
        if (data.getRole().equals(1)){
            // 等效转交所有权
            // 有且一人
            if (data.getLinkIds().size()>1){
                throw new CustomException("转交所有权只能选择一人");
            }
            if (data.getLinkType().equals(2)){
                throw new CustomException("禁止转交所有权给部门");
            }
        }
        List<Long> linkIds = new ArrayList<>();
        for (String id : data.getLinkIds()) {
            linkIds.add(Long.valueOf(StringUtils.trim(id)));
        }
        UserInfo currentUser = ThreadLocalUtil.getCurrentUser();
        for (Long id : linkIds) {
            if (id.equals(currentUser.getId())){
                throw new CustomException("请勿操作自己");
            }
        }
        LambdaUpdateWrapper<SysPrintDeviceLink> sysPrintDeviceUserLambdaQueryWrapper = new LambdaUpdateWrapper<>();
        sysPrintDeviceUserLambdaQueryWrapper.eq(SysPrintDeviceLink::getPrintDeviceId,byId.getId());
        sysPrintDeviceUserLambdaQueryWrapper.eq(SysPrintDeviceLink::getLinkType,data.getLinkType());
        if (data.getLinkIds().size()>1){
            sysPrintDeviceUserLambdaQueryWrapper.in(SysPrintDeviceLink::getLinkId,linkIds);
        }else {
            sysPrintDeviceUserLambdaQueryWrapper.eq(SysPrintDeviceLink::getLinkId,linkIds.get(0));
        }
        sysPrintDeviceUserLambdaQueryWrapper.set(SysPrintDeviceLink::getRole,data.getRole());
        sysPrintDeviceLinkDao.update(sysPrintDeviceUserLambdaQueryWrapper);
        if (data.getRole().equals(1)){
            // 将自己的所有者更新成link
            LambdaUpdateWrapper<SysPrintDeviceLink> selfUpdate = new LambdaUpdateWrapper<>();
            selfUpdate.eq(SysPrintDeviceLink::getLinkId,currentUser.getId());
            selfUpdate.eq(SysPrintDeviceLink::getLinkType,data.getLinkType());
            selfUpdate.eq(SysPrintDeviceLink::getPrintDeviceId,byId.getId());
            selfUpdate.set(SysPrintDeviceLink::getRole,3);
            sysPrintDeviceLinkDao.update(selfUpdate);
        }
        return "更新关联成功";
    }


    @Override
    public List<PrintDeviceVO> getPrintDeviceList() {
        UserInfo currentUser = ThreadLocalUtil.getCurrentUser();
        LambdaQueryWrapper<SysPrintDeviceLink> sysPrintDeviceUserLambdaQueryWrapper = new LambdaQueryWrapper<>();
        sysPrintDeviceUserLambdaQueryWrapper.eq(SysPrintDeviceLink::getLinkId,currentUser.getId());
        sysPrintDeviceUserLambdaQueryWrapper.eq(SysPrintDeviceLink::getLinkType,1);
        sysPrintDeviceUserLambdaQueryWrapper.or(sysPrintDeviceUserLambdaQueryWrapper1 -> {
            sysPrintDeviceUserLambdaQueryWrapper1.eq(SysPrintDeviceLink::getLinkType,2).
                    eq(SysPrintDeviceLink::getLinkId,currentUser.getDeptId());
        });
        List<SysPrintDeviceLink> list = sysPrintDeviceLinkDao.list(sysPrintDeviceUserLambdaQueryWrapper);
        List<PrintDeviceVO> printDeviceVOS = new ArrayList<>();

        for (SysPrintDeviceLink sysPrintDeviceLink : list) {
            Long printDeviceId = sysPrintDeviceLink.getPrintDeviceId();
            SysPrintDevice sysPrintDevice = sysPrintDeviceDao.getById(printDeviceId);
            if (sysPrintDevice==null){
                continue;
                // 可能设备已删除
            }
            LambdaQueryWrapper<SysPrintDeviceLink> ownerSDUL = new LambdaQueryWrapper<>();
            ownerSDUL.eq(SysPrintDeviceLink::getRole,1);
            ownerSDUL.eq(SysPrintDeviceLink::getLinkType,1);
            ownerSDUL.eq(SysPrintDeviceLink::getPrintDeviceId,sysPrintDevice.getId());
            SysPrintDeviceLink ownerSDU = sysPrintDeviceLinkDao.getOne(ownerSDUL);
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
            printDeviceVO.setUserRole(sysPrintDeviceLink.getRole());
            printDeviceVO.setId(sysPrintDevice.getId());
            printDeviceVOS.add(printDeviceVO);
        }
        return printDeviceVOS;
    }


}
