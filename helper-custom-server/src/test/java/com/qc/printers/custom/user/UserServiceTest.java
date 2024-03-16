package com.qc.printers.custom.user;

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

public class UserServiceTest {
    @Autowired
    private UserService userService;

    @Test
    public void helloTest() throws Exception {
        UserSelectListResp userSelectListResp = userService.userSelectOnlyXUserList(1700781419697745922L);
        log.info("userSelectListResp{}", userSelectListResp);
    }
}
