package com.qc.printers.common.print.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ecwid.consul.v1.health.model.HealthService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qc.printers.common.common.MyString;
import com.qc.printers.common.common.R;
import com.qc.printers.common.common.service.ConsulService;
import com.qc.printers.common.common.utils.FileMD5;
import com.qc.printers.common.common.utils.RedisUtils;
import com.qc.printers.common.print.domain.dto.PrintDeviceDto;
import com.qc.printers.common.print.domain.entity.PrintDevice;
import com.qc.printers.common.print.domain.entity.PrintDocumentTypeStatistic;
import com.qc.printers.common.print.domain.entity.Printer;
import com.qc.printers.common.print.mapper.PrinterMapper;
import com.qc.printers.common.print.service.IPrinterService;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
public class IPrinterServiceImpl extends ServiceImpl<PrinterMapper, Printer> implements IPrinterService {

    @Autowired
    private IPrinterService iPrinterService;

    @Autowired
    private ConsulService consulService;

    @Transactional
    @Override
    public boolean addPrinter(Printer printer, String urlName) {
        if (StringUtils.isEmpty(printer.getName())) {
            log.error("打印记录缺失");
            return false;
        }
        if (printer.getCreateUser() == null) {
            log.error("打印记录缺失");
            return false;
        }

        InputStream inputStream = null;
        try {
            URL url = new URL(urlName);
            inputStream = url.openStream();
            printer.setContentHash(FileMD5.md5HashCode(inputStream));
            return iPrinterService.save(printer);
        } catch (FileNotFoundException e) {
            log.error("打印记录缺失");
            log.error(e.getMessage());
            return false;
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                assert inputStream != null;
                inputStream.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public PrintDeviceDto pollingPrintDevice(String printId) {
        PrintDeviceDto printDeviceDto = RedisUtils.hget(MyString.printDevice, printId, PrintDeviceDto.class);
        LocalDateTime currentTime = LocalDateTime.now();


        if (printDeviceDto != null && Duration.between(currentTime, printDeviceDto.getLastTime()).abs().getSeconds() < 3) {
            // 存在记录且在3s内,要求3s更新
            return printDeviceDto;
        }
        // 不符合条件，，直接更新并覆盖，如果没有这个设备则返回null
        List<HealthService> registeredServices = consulService.getRegisteredServices();
        Optional<HealthService> first = registeredServices.stream().filter(obj -> obj.getService().getId().equals(printId)).findFirst();
        if (first.isEmpty()) {
            return null;
        }
        PrintDeviceDto device = new PrintDeviceDto();
        device.setPort(first.get().getService().getPort());
        device.setLastTime(currentTime);
        device.setIp(first.get().getService().getAddress());
        device.setPrintDescription(first.get().getService().getMeta().get("ZName"));
        device.setPrintName(device.getPrintDescription());
        device.setId(first.get().getService().getId());
        //筛选了只要状态正常的，所以这里全是正常的
        device.setStatusType(1);
        //获取详情信息
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("http://" + device.getIp() + ":" + device.getPort() + "/api/printDevice/status") // 替换为实际API的URL
                .build();
        try {
            Response response = client.newCall(request).execute();
            if (response.isSuccessful()) {
                ObjectMapper objectMapper = new ObjectMapper();
                String jsonResponse = response.body().string();

                // 使用Jackson库将JSON字符串解析为List<MyObject>
                R<PrintDevice> yuandata = objectMapper.readValue(jsonResponse, new TypeReference<R<PrintDevice>>() {
                });
                if (yuandata == null) {
                    return null;
                }
                if (!yuandata.getCode().equals(1)) {
                    return null;
                }
                PrintDevice data = yuandata.getData();
                device.setStatusType(data.getStatusType());
                device.setListNums(data.getListNums());
                device.setPrintJobs(data.getPrintJobs());
                device.setPrintName(data.getPrintName());
                device.setStatusTypeMessage(data.getStatusTypeMessage());
                device.setPrintDescription(data.getPrintDescription());
                RedisUtils.hset(MyString.printDevice, device.getId(), device, 300);
                return device;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return null;
    }

    @Override
    public List<PrintDocumentTypeStatistic> getPrinterTypeStatistics() {
        // 全部打印信息
        List<Printer> list = iPrinterService.list(new LambdaQueryWrapper<Printer>().select(Printer::getId, Printer::getName));
        List<PrintDocumentTypeStatistic> printDocumentTypeStatisticList = new ArrayList<>();
        Map<String, Integer> typeNumbers = new HashMap<>();
        for (Printer printer : list) {
            String fileName = printer.getName();
            int lastDotIndex = fileName.lastIndexOf(".");
            String extension = fileName.substring(lastDotIndex + 1);
            //java8:特性 如果key存在了，就返回key对应的object计算后的值，如果不存在就返回value，同时更新map
            typeNumbers.merge(extension, 1, Integer::sum);
        }
        for (String key : typeNumbers.keySet()) {
            Integer value = typeNumbers.get(key);
            // 做一些操作，例如打印键值对
            PrintDocumentTypeStatistic printDocumentTypeStatistic = new PrintDocumentTypeStatistic();
            printDocumentTypeStatistic.setType(key);
            printDocumentTypeStatistic.setCount(value);
            printDocumentTypeStatistic.setProportion(value / (list.size() * 1.0));
            printDocumentTypeStatisticList.add(printDocumentTypeStatistic);
        }
        return printDocumentTypeStatisticList;
    }
}
