package com.qc.printers.common.vailcode.service.strategy;


import com.qc.printers.common.common.CustomException;
import com.qc.printers.common.common.MyString;
import com.qc.printers.common.common.utils.RedisUtils;
import com.qc.printers.common.common.utils.SpElUtils;
import com.qc.printers.common.vailcode.annotations.CheckVailCode;
import com.qc.printers.common.vailcode.config.VailCodeConfig;
import com.qc.printers.common.vailcode.domain.enums.VailType;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.reflect.Method;

@Service
@Slf4j
public class VerifyVailCodeTypeHandel extends VailCodeVerifyHandel {

    @Autowired
    private VailCodeConfig vailCodeConfig;

    @Override
    VailType getVailTypeEnum() {
        return VailType.VAILCODE;
    }

    @Override
    public void verify(Method method, Object[] args, CheckVailCode checkVailCode) {
        if (!vailCodeConfig.isUse()) {
            return;
        }
        // 该项只需要code
        String code = SpElUtils.parseSpEl(method, args, checkVailCode.key());

        String zhenvalue = RedisUtils.get(MyString.CAPTCHA_Success + code);
        if (StringUtils.isEmpty(zhenvalue)) {
            throw new CustomException("请重试，滑动验证码过期了");
        }
        RedisUtils.del(MyString.CAPTCHA_Success + code);
    }
}
