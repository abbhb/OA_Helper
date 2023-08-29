package com.qc.printers.common.print.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qc.printers.common.common.utils.FileMD5;
import com.qc.printers.common.print.domain.entity.PrintDocumentTypeStatistic;
import com.qc.printers.common.print.domain.entity.Printer;
import com.qc.printers.common.print.mapper.PrinterMapper;
import com.qc.printers.common.print.service.IPrinterService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class IPrinterServiceImpl extends ServiceImpl<PrinterMapper, Printer> implements IPrinterService {

    @Autowired
    private IPrinterService iPrinterService;


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
