package com.qc.printers.custom.ldap;

import com.qc.printers.common.ldap.service.LdapService;
import com.qc.printers.common.signin.mapper.SigninRenewalMapper;
import com.qc.printers.custom.user.domain.vo.response.UserSelectListResp;
import com.qc.printers.custom.user.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
public class LdapTest {
    @Autowired
    private LdapService ldapService;

    @Test
    public void ldapSyncTest() throws Exception {
        ldapService.syncDataToLdap();
    }
}
