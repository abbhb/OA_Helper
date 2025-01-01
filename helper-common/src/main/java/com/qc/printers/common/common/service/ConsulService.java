package com.qc.printers.common.common.service;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.QueryParams;
import com.ecwid.consul.v1.health.model.HealthService;
import com.qc.printers.common.config.ConsulConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class ConsulService {
    private final ConsulConfig consulConfig;
    private ConsulClient consulClient;

    @Autowired
    public ConsulService(ConsulConfig consulConfig) {
        this.consulConfig = consulConfig;
        consulClient = new ConsulClient(consulConfig.getIp(), consulConfig.getPort());
    }


    public List<HealthService> getRegisteredServices(String serviceName, Boolean onluPassing) {
        // 使用 Consul 客户端获取已注册的服务
        List<HealthService> healthyServices = consulClient.getHealthServices(serviceName, onluPassing, QueryParams.DEFAULT).getValue();
        // 过滤出健康的服务实例
        return healthyServices;
    }

    public List<HealthService> getFace2ArrayServices(){
        return getRegisteredServices(consulConfig.getSigninConsulName(),true);
    }
    public List<HealthService> getPrintDeviceServices(){
        return getRegisteredServices(consulConfig.getPrintDeviceConsulName(),true);
    }
    public List<HealthService> getSigninServices(){
        return getRegisteredServices(consulConfig.getSigninConsulName(),true);
    }
}
