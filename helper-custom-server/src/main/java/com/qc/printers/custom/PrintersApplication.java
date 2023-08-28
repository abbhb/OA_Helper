package com.qc.printers.custom;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qc.printers.common.common.MyString;
import com.qc.printers.common.common.service.CommonConfigService;
import com.qc.printers.common.common.utils.RSAUtil;
import com.qc.printers.common.common.utils.RedisUtils;
import com.qc.printers.common.user.domain.entity.Permission;
import com.qc.printers.common.user.mapper.PermissionMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.List;

//@EnableOpenApi//启动swaggerUI
@SpringBootApplication(scanBasePackages = {"com.qc.printers"})
@Slf4j
public class PrintersApplication implements CommandLineRunner {

    @Autowired
    private PermissionMapper permissionMapper;

    @Autowired
    private CommonConfigService commonConfigService;

    public static void main(String[] args) {
        SpringApplication.run(PrintersApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        //启动成功执行该方法
        log.info("启动主程序");
        //对权限进行缓存
        LambdaQueryWrapper<Permission> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        List<Permission> permissions = permissionMapper.selectList(lambdaQueryWrapper);
        for (Permission permission :
                permissions) {
            RedisUtils.hset(MyString.permission_key, String.valueOf(permission.getId()), permission);
        }
        //缓存公共配置
        commonConfigService.list();
        //创建rsa的key
        RSAUtil.createKey();
    }
}
