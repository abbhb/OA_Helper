package com.qc.printers.custom.printer;

import com.qc.printers.common.print.domain.vo.request.PreUploadPrintFileReq;
import com.qc.printers.common.print.domain.vo.request.UpdatePrintDeviceStatusReq;
import com.qc.printers.custom.print.controller.PrintDeviceManagerController;
import com.qc.printers.custom.print.service.PrinterService;
import com.qc.printers.utils.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;


@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
public class PrintDeviceManagerTest {

    @Autowired
    private PrintDeviceManagerController printDeviceManagerController;

    private MockMvc mockMvc;

    @Before
    public void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(printDeviceManagerController).build();
    }


    @Test
    public void printDeviceUpdateStatusTest() throws Exception {
        UpdatePrintDeviceStatusReq updatePrintDeviceStatusReq = new UpdatePrintDeviceStatusReq();
        updatePrintDeviceStatusReq.setId("1");
        updatePrintDeviceStatusReq.setStatus(0);

        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.put("/print_device/update_status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtils.toStr(updatePrintDeviceStatusReq))
                                .header("Authorization", "Bearer test-token")
                )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andDo(MockMvcResultHandlers.print())
                .andReturn();

        log.info(mvcResult.getResponse().getContentAsString());
    }
}
