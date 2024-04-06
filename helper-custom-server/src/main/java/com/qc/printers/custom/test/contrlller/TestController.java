package com.qc.printers.custom.test.contrlller;

import com.qc.printers.common.common.R;
import com.qc.printers.common.user.dao.UserDao;
import com.qc.printers.transaction.service.MQProducer;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@RestController//@ResponseBody+@Controller
@RequestMapping("/test")
@Api("TestURL")
@CrossOrigin("*")
@Slf4j
public class TestController {
    @Autowired
    private MQProducer mqProducer;

    @Autowired
    private UserDao userDao;

    @GetMapping("/test01")
    public R<String> test01(String msg) throws IOException {
//        mqProducer.sendMessageWithTags("test", "测试:" + msg, "test");

        InputStream io = getClass().getClassLoader().getResourceAsStream("templates/verificationEmail.html");
        String fileContent = new String(io.readAllBytes(), StandardCharsets.UTF_8);
        io.close();
        List<String> list = Arrays.asList("123456".split(""));
        String replacedContent = fileContent.replace("[[${code1}]]", list.get(0));
        replacedContent = replacedContent.replace("[[${code2}]]", list.get(1));
        replacedContent = replacedContent.replace("[[${code3}]]", list.get(2));
        replacedContent = replacedContent.replace("[[${code4}]]", list.get(3));
        replacedContent = replacedContent.replace("[[${code5}]]", list.get(4));
        replacedContent = replacedContent.replace("[[${code6}]]", list.get(5));
        return R.success(replacedContent);
    }

    @GetMapping("/test02")
    public R<String> test02(String msg, String url) {
//        TestJson testJson = new TestJson();
//        testJson.setId(1L);
//        testJson.setName("21312");
//        testJson.setUrl(url);
//        testJson.setMessage(msg);
//        mqProducer.sendMessageWithTags("print_filetopdf_send_msg", testJson, "req");
//        return R.success("test-Json成功");
//        List<User> list = userDao.list();
//        for (User user : list) {
//            if (StringUtils.isEmpty(user.getOpenId())){
//                user.setOpenId(UUID.randomUUID().toString());
//                userDao.updateById(user);
//            }
//        }
        log.info("hour{},mi{},s：{}", LocalDateTime.now().getHour(), LocalDateTime.now().getMinute(), LocalDateTime.now().getSecond());
        return R.success("");
    }
}
