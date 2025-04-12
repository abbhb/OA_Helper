package com.qc.printers.common.ikuai.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qc.printers.common.common.CustomException;
import com.qc.printers.common.common.domain.entity.PageData;
import com.qc.printers.common.common.utils.StringUtils;
import com.qc.printers.common.common.utils.ThreadLocalUtil;
import com.qc.printers.common.ikuai.dao.SysIkuaiNetAllowDao;
import com.qc.printers.common.ikuai.domain.dto.SysIkuaiNetAllowLinkDto;
import com.qc.printers.common.ikuai.domain.entity.SysIkuaiNetAllow;
import com.qc.printers.common.ikuai.domain.vo.req.IkuaiAllowLinkQuery;
import com.qc.printers.common.ikuai.domain.vo.req.IkuaiAllowLinkReq;
import com.qc.printers.common.print.domain.entity.SysPrintDevice;
import com.qc.printers.common.print.domain.entity.SysPrintDeviceLink;
import com.qc.printers.common.user.dao.UserDao;
import com.qc.printers.common.user.domain.dto.UserInfo;
import com.qc.printers.common.user.domain.entity.SysDept;
import com.qc.printers.common.user.domain.entity.User;
import com.qc.printers.common.user.service.ISysDeptService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Slf4j
public class IkuaiService {
    @Autowired
    private SysIkuaiNetAllowDao sysIkuaiNetAllowDao;

    @Autowired
    private UserDao userDao;

    @Autowired
    private ISysDeptService iSysDeptService;
    public PageData<SysIkuaiNetAllowLinkDto> getIkuaiAllowLinks(IkuaiAllowLinkQuery params) {
        Page<SysIkuaiNetAllow> pageInfo = new Page<>(params.getPageNum(), params.getPageSize());
        PageData<SysIkuaiNetAllowLinkDto> pageData = new PageData<>();
        LambdaQueryWrapper<SysIkuaiNetAllow> sysPrintDeviceUserLambdaQueryWrapper = new LambdaQueryWrapper<>();
        sysPrintDeviceUserLambdaQueryWrapper.orderByDesc(SysIkuaiNetAllow::getLinkType);
        sysIkuaiNetAllowDao.page(pageInfo,sysPrintDeviceUserLambdaQueryWrapper);
        List<SysIkuaiNetAllowLinkDto> results = new ArrayList<>();
        for (SysIkuaiNetAllow sysIkuaiNetAllow : pageInfo.getRecords()) {
            SysIkuaiNetAllowLinkDto sysIkuaiNetAllowLinkDto = new SysIkuaiNetAllowLinkDto();
            sysIkuaiNetAllowLinkDto.setLinkId(sysIkuaiNetAllow.getLinkId());
            sysIkuaiNetAllowLinkDto.setId(sysIkuaiNetAllow.getId());
            // 1:user 2:dept
            sysIkuaiNetAllowLinkDto.setLinkType(sysIkuaiNetAllow.getLinkType());
            if (sysIkuaiNetAllow.getLinkType().equals(1)){
                User byId = userDao.getById(sysIkuaiNetAllow.getLinkId());
                if (byId==null){
                    continue;
                }
                sysIkuaiNetAllowLinkDto.setLinkName(byId.getName());
            }else if (sysIkuaiNetAllow.getLinkType().equals(2)){
                SysDept sysDept = iSysDeptService.getById(sysIkuaiNetAllow.getLinkId());
                if (sysDept==null){
                    continue;
                }
                sysIkuaiNetAllowLinkDto.setLinkName("[用户组]"+sysDept.getDeptNameAll());
            }
            results.add(sysIkuaiNetAllowLinkDto);
        }
        pageData.setPages(pageInfo.getPages());
        pageData.setTotal(pageInfo.getTotal());
        pageData.setCurrent(pageInfo.getCurrent());
        pageData.setSize(pageInfo.getSize());
        pageData.setRecords(results);
        return pageData;
    }

    public Map<Integer, List<Long>> getIkuaiAllowLinkIds() {

        LambdaQueryWrapper<SysIkuaiNetAllow> ikuaiNetAllowLambdaQueryWrapper = new LambdaQueryWrapper<>();
        ikuaiNetAllowLambdaQueryWrapper.select(SysIkuaiNetAllow::getLinkId,SysIkuaiNetAllow::getLinkType);

        List<SysIkuaiNetAllow> list = sysIkuaiNetAllowDao.list(ikuaiNetAllowLambdaQueryWrapper);
        Map<Integer, List<Long>> listMap = new HashMap<>();
        listMap.put(1,list.stream().filter(sysPrintDeviceLink -> sysPrintDeviceLink.getLinkType().equals(1)).map(SysIkuaiNetAllow::getLinkId).toList());
        listMap.put(2,list.stream().filter(sysPrintDeviceLink -> sysPrintDeviceLink.getLinkType().equals(2)).map(SysIkuaiNetAllow::getLinkId).toList());
        return listMap;
    }

    public String addIkuaiAllowLinks(IkuaiAllowLinkReq data) {
        if (data.getLinkIds()==null|| data.getLinkIds().isEmpty()){
            throw new CustomException("必须包含要添加的用户");
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
            LambdaQueryWrapper<SysIkuaiNetAllow> NotFoundLambdaQueryWrapper = new LambdaQueryWrapper<>();
            NotFoundLambdaQueryWrapper.eq(SysIkuaiNetAllow::getLinkId,l);
            NotFoundLambdaQueryWrapper.eq(SysIkuaiNetAllow::getLinkType,data.getLinkType());

            int count = (int) sysIkuaiNetAllowDao.count(NotFoundLambdaQueryWrapper);
            if (count>0){
                continue;
            }
            linkIdResultList.add(l);
        }


        for (Long linkId : linkIdResultList) {
            SysIkuaiNetAllow build = SysIkuaiNetAllow.builder()
                    .linkType(data.getLinkType())
                    .linkId(linkId)
                    .build();
            sysIkuaiNetAllowDao.save(build);
        }
        return "添加成功";
    }

    public String removeIkuaiAllowLink(String linkId, Integer linkType) {
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
        // 构造批量删除绑定关系
        LambdaQueryWrapper<SysIkuaiNetAllow> sysPrintDeviceUserLambdaQueryWrapper = new LambdaQueryWrapper<>();
        sysPrintDeviceUserLambdaQueryWrapper.eq(SysIkuaiNetAllow::getLinkType,linkType);
        if (linkIds.size()>1){
            sysPrintDeviceUserLambdaQueryWrapper.in(SysIkuaiNetAllow::getLinkId,linkIds);
        }else {
            sysPrintDeviceUserLambdaQueryWrapper.eq(SysIkuaiNetAllow::getLinkId,linkIds.get(0));
        }
        // 删除
        sysIkuaiNetAllowDao.remove(sysPrintDeviceUserLambdaQueryWrapper);
        return "删除成功";
    }
}
