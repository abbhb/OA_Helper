package com.qc.printers.custom.user;

import com.qc.printers.common.signin.domain.entity.SigninRenewal;
import com.qc.printers.common.signin.mapper.SigninRenewalMapper;
import com.qc.printers.custom.user.domain.vo.response.UserSelectListResp;
import com.qc.printers.custom.user.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)

public class UserServiceTest {
    @Autowired
    private UserService userService;
    @Autowired
    private SigninRenewalMapper signinRenewalMapper;

    @Test
    public void helloTest() throws Exception {
        UserSelectListResp userSelectListResp = userService.userSelectOnlyXUserList(1700781419697745922L);
        log.info("userSelectListResp{}", userSelectListResp);
    }
    @Test
    public void helloTest2() throws Exception {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        SigninRenewal signinRenewal = signinRenewalMapper.hasExistRenewal(LocalDateTime.parse("2024-09-15 15:00:00",dateTimeFormatter), LocalDateTime.parse("2024-09-15 16:00:00",dateTimeFormatter), 1L);
        log.info("signinRenewal{}", signinRenewal);
    }
}
