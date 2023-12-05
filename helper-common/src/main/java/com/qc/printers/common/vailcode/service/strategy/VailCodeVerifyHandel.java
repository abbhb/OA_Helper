package com.qc.printers.common.vailcode.service.strategy;

import com.qc.printers.common.vailcode.annotations.CheckVailCode;
import com.qc.printers.common.vailcode.config.VailCodeConfig;
import com.qc.printers.common.vailcode.domain.enums.VailType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.reflect.Method;


@Service
@Slf4j
public abstract class VailCodeVerifyHandel {

    @Autowired
    private VailCodeConfig vailCodeConfig;

    abstract VailType getVailTypeEnum();

    public abstract void verify(Method method, Object[] args, CheckVailCode checkVailCode);


}
