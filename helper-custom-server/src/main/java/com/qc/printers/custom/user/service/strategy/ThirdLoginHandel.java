package com.qc.printers.custom.user.service.strategy;


import com.qc.printers.custom.user.domain.enums.ThirdLoginEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Service
@Slf4j
public abstract class ThirdLoginHandel {

    abstract ThirdLoginEnum getDataTypeEnum();


    public abstract void thirdLoginHandel(HttpServletRequest request, HttpServletResponse response);
}
