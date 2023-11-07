package com.qc.printers.common.oauth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.qc.printers.common.common.CustomException;
import com.qc.printers.common.oauth.dao.SysOauthDao;
import com.qc.printers.common.oauth.dao.SysOauthUserDao;
import com.qc.printers.common.oauth.domain.entity.SysOauth;
import com.qc.printers.common.oauth.domain.entity.SysOauthUser;
import com.qc.printers.common.oauth.service.OauthMangerService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
@Service
@Slf4j
public class OauthMangerServiceImpl implements OauthMangerService {
    @Autowired
    private SysOauthDao sysOauthDao;

    @Autowired
    private SysOauthUserDao sysOauthUserDao;

    @Override
    public String addOauth(SysOauth sysOauth) {
        if (StringUtils.isEmpty(sysOauth.getClientId())) {
            throw new CustomException("clientId不能为空");
        }
        if (StringUtils.isEmpty(sysOauth.getClientName())) {
            throw new CustomException("clientName不能为空");
        }
        if (StringUtils.isEmpty(sysOauth.getClientSecret())) {
            throw new CustomException("clientSecret不能为空");
        }
        if (StringUtils.isEmpty(sysOauth.getDomainName())) {
            throw new CustomException("DomainName不能为空");
        }
        if (sysOauth.getNoSertRedirect() == null) {
            throw new CustomException("NoSertRedirect不能为空");
        }
        if (sysOauth.getGrantType() == null) {
            throw new CustomException("GrantType不能为空");
        }
        if (sysOauth.getStatus() == null) {
            throw new CustomException("Status不能为空");
        }
        sysOauthDao.save(sysOauth);

        return "添加成功";
    }

    @Override
    public String deleteOauth(Long id) {
        if (id == null) {
            throw new CustomException("Id不能为空");
        }
        LambdaQueryWrapper<SysOauthUser> sysOauthUserLambdaQueryWrapper = new LambdaQueryWrapper<>();
        sysOauthUserLambdaQueryWrapper.eq(SysOauthUser::getOauthId, id);
        sysOauthUserDao.remove(sysOauthUserLambdaQueryWrapper);
        sysOauthDao.removeById(id);
        return "删除成功";
    }

    @Override
    public String updateOauth(SysOauth sysOauth) {
        if (sysOauth.getId() == null) {
            throw new CustomException("Id不能为空");
        }
        if (StringUtils.isEmpty(sysOauth.getClientId())) {
            throw new CustomException("clientId不能为空");
        }
        if (StringUtils.isEmpty(sysOauth.getClientName())) {
            throw new CustomException("clientName不能为空");
        }
        if (StringUtils.isEmpty(sysOauth.getClientSecret())) {
            throw new CustomException("clientSecret不能为空");
        }
        if (StringUtils.isEmpty(sysOauth.getDomainName())) {
            throw new CustomException("DomainName不能为空");
        }
        if (sysOauth.getNoSertRedirect() == null) {
            throw new CustomException("NoSertRedirect不能为空");
        }
        if (sysOauth.getGrantType() == null) {
            throw new CustomException("GrantType不能为空");
        }
        if (sysOauth.getStatus() == null) {
            throw new CustomException("Status不能为空");
        }
        sysOauthDao.updateById(sysOauth);
        return "更新成功";
    }


    @Override
    public String changeOauthStatus(SysOauth sysOauth) {
        if (sysOauth.getId() == null) {
            throw new CustomException("Id不能为空");
        }
        if (sysOauth.getStatus() == null) {
            throw new CustomException("Status不能为空");
        }
        LambdaUpdateWrapper<SysOauth> sysOauthLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        sysOauthLambdaUpdateWrapper.set(SysOauth::getStatus, sysOauth.getStatus());
        sysOauthLambdaUpdateWrapper.eq(SysOauth::getId, sysOauth.getId());
        sysOauthDao.update(sysOauthLambdaUpdateWrapper);
        return "更改成功";
    }

    @Override
    public List<SysOauth> listOauth() {
        return sysOauthDao.list();
    }

    @Override
    public SysOauth queryOauth(Long oauthId) {
        return sysOauthDao.getById(oauthId);
    }

}
