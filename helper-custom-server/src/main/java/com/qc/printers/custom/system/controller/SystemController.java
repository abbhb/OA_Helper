package com.qc.printers.custom.system.controller;

import com.qc.printers.common.common.Code;
import com.qc.printers.common.common.CustomException;
import com.qc.printers.common.common.R;
import com.qc.printers.common.common.annotation.NeedToken;
import com.qc.printers.common.common.annotation.PermissionCheck;
import com.qc.printers.common.ldap.domain.vo.SyncLdapJobVO;
import com.qc.printers.common.ldap.service.LdapService;
import com.qc.printers.custom.user.domain.vo.response.ThirdCallbackResp;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController//@ResponseBody+@Controller
@RequestMapping("/system_control")
@Api("系统预留控制器")
@CrossOrigin("*")
@Slf4j
public class SystemController {
    @Autowired
    private LdapService ldapService;
    @GetMapping("/sync_mysql_ldap")
    @NeedToken
    @PermissionCheck(role = {"superadmin"}, permission = "sys:system:sync_ldap")
    @ApiOperation(value = "同步mysql用户到ldap", notes = "")
    public R<Void> syncMysqlToLdap() {
        ldapService.syncDataToLdap();
        return R.successOnlyMsg("success", Code.SUCCESS);
    }
    @GetMapping("/sync_mysql_ldap_jobs")
    @NeedToken
    @PermissionCheck(role = {"superadmin"}, permission = "sys:system:sync_ldap")
    @ApiOperation(value = "同步mysql用户到ldap的任务", notes = "")
    public R<List<SyncLdapJobVO>> syncMysqlToLdapJobs() {
        return R.success(ldapService.syncDataToLdapJobs());
    }
}
