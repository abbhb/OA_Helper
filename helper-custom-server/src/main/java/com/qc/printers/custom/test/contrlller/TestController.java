package com.qc.printers.custom.test.contrlller;

import com.qc.printers.common.common.R;
import com.qc.printers.custom.test.entity.TestJson;
import com.qc.printers.transaction.service.MQProducer;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController//@ResponseBody+@Controller
@RequestMapping("/test")
@Api("TestURL")
@CrossOrigin("*")
@Slf4j
public class TestController {
    @Autowired
    private MQProducer mqProducer;

    @GetMapping("/test01")
    public R<String> test01(String msg) {
        mqProducer.sendMessageWithTags("test", "测试:" + msg, "test");
        return R.success("test成功");
    }

    @GetMapping("/test02")
    public R<String> test02(String msg, String url) {
        TestJson testJson = new TestJson();
        testJson.setId(1L);
        testJson.setName("21312");
        testJson.setUrl(url);
        testJson.setMessage(msg);
        mqProducer.sendMessageWithTags("print_filetopdf_send_msg", testJson, "req");
        return R.success("test-Json成功");
    }
}
