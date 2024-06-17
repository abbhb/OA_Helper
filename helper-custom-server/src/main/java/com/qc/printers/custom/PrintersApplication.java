package com.qc.printers.custom;

import com.qc.printers.common.common.service.init.InitServer;
import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

//@EnableOpenApi//启动swaggerUI
@EnableAspectJAutoProxy(exposeProxy = true)
@SpringBootApplication(scanBasePackages = {"com.qc.printers"}, exclude = {org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class})
@MapperScan({"com.qc.printers.common.**.mapper"})
@Slf4j
public class PrintersApplication implements CommandLineRunner {


    @Autowired
    private InitServer initServer;

    public static void main(String[] args) {
        SpringApplication.run(PrintersApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        //启动成功执行该方法
        log.info("启动主程序");
        initServer.init();
    }
}
