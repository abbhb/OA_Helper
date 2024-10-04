package com.qc.printers.custom.user;

import cn.hutool.json.JSONUtil;
import com.qc.printers.common.print.domain.vo.response.UnoServiceInfo;
import com.qc.printers.common.signin.domain.resp.FaceFileResp;
import com.qc.printers.custom.print.service.PrinterService;
import com.qc.printers.custom.print.service.impl.PrinterServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
public class JsonTest {

    @Autowired
    private PrinterService printerService;
    @Test
    public void testString() {
        String s = "[{\"name\":\"QQ图片20231221133340.jpg\",\"url\":\"aistudio/QQ%E5%9B%BE%E7%89%8720231221133341716202700911.jpg\"}]";
        List<FaceFileResp> list = JSONUtil.toList(s, FaceFileResp.class);
        log.info("{}", list);
    }

    @Test
    public void testString2() {
        boolean b = PrinterServiceImpl.fileExists("aistudio/pdf/2024-10/1842153300155273218/251d5170-d554-4e4f-8b15-14d918a499e4.pdf");
//        List<FaceFileResp> list = JSONUtil.toList(s, FaceFileResp.class);
        log.info("{}", b);
    }
    @Test
    public void testString3() {
        UnoServiceInfo unoServiceInfo = printerService.unoServiceInfo();
//        List<FaceFileResp> list = JSONUtil.toList(s, FaceFileResp.class);
        log.info("{}", unoServiceInfo);
    }

}
