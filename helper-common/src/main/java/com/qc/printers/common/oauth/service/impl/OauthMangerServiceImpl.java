package com.qc.printers.common.oauth.service.impl;

import com.qc.printers.common.oauth.dao.SysOauthDao;
import com.qc.printers.common.oauth.dao.SysOauthPermissionBindDao;
import com.qc.printers.common.oauth.dao.SysOauthPermissionDao;
import com.qc.printers.common.oauth.domain.dto.SysOauthInfoDto;
import com.qc.printers.common.oauth.domain.entity.SysOauth;
import com.qc.printers.common.oauth.domain.entity.SysOauthPermission;
import com.qc.printers.common.oauth.domain.entity.SysOauthPermissionBind;
import com.qc.printers.common.oauth.domain.enums.OauthErrorEnum;
import com.qc.printers.common.oauth.exception.OauthException;
import com.qc.printers.common.oauth.service.OauthMangerService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class OauthMangerServiceImpl implements OauthMangerService {
    @Autowired
    private SysOauthDao sysOauthDao;

    @Autowired
    private SysOauthPermissionDao sysOauthPermissionDao;

    @Autowired
    private SysOauthPermissionBindDao sysOauthPermissionBindDao;

    @Override
    public String addOauthPermission(SysOauthPermission sysOauthPermission) {
        if (sysOauthPermission.getIntro() == null) {
            throw new OauthException(OauthErrorEnum.OAUTH_ERROR, "intro不能为空");
        }
        if (StringUtils.isEmpty(sysOauthPermission.getKey())) {
            throw new OauthException(OauthErrorEnum.OAUTH_ERROR, "key不能为空");
        }
        if (sysOauthPermission.getIsMust() == null) {
            throw new OauthException(OauthErrorEnum.OAUTH_ERROR, "是否为必选不能为空");
        }
        sysOauthPermission.setId(null);
        sysOauthPermissionDao.save(sysOauthPermission);
        return "添加成功";
    }

    @Override
    public String updateOauthPermission(SysOauthPermission sysOauthPermission) {
        if (sysOauthPermission.getId() == null) {
            throw new OauthException(OauthErrorEnum.OAUTH_ERROR, "id不能为空");
        }
        if (sysOauthPermission.getIntro() == null) {
            throw new OauthException(OauthErrorEnum.OAUTH_ERROR, "intro不能为空");
        }
        if (StringUtils.isEmpty(sysOauthPermission.getKey())) {
            throw new OauthException(OauthErrorEnum.OAUTH_ERROR, "key不能为空");
        }
        if (sysOauthPermission.getIsMust() == null) {
            throw new OauthException(OauthErrorEnum.OAUTH_ERROR, "是否为必选不能为空");
        }
        sysOauthPermissionDao.updateById(sysOauthPermission);
        return "更新成功";
    }

    @Override
    public String deleteOauthPermission(Long id) {

        return null;
    }

    @Override
    public SysOauthPermission getOauthPermission(Long id) {
        return null;
    }

    @Override
    public List<SysOauthPermission> listOauthPermission() {
        return null;
    }

    @Override
    public String addOauth(SysOauth sysOauth, List<SysOauthPermissionBind> sysOauthPermissionBinds) {
        return null;
    }

    @Override
    public String deleteOauth(Long id) {
        return null;
    }

    @Override
    public String updateOauth(SysOauth sysOauth, List<SysOauthPermissionBind> sysOauthPermissionBinds) {
        return null;
    }

    @Override
    public String changeOauthStatus(SysOauth sysOauth) {
        return null;
    }

    @Override
    public List<SysOauthInfoDto> listOauth() {
        return null;
    }

    @Override
    public SysOauthInfoDto queryOauth() {
        return null;
    }
}
