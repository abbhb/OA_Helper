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
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Slf4j
@Component
public class VerifyEmailCodeTypeHandel extends VailCodeVerifyHandel {

    @Autowired
    private VailCodeConfig vailCodeConfig;

    @Override
    VailType getVailTypeEnum() {
        return VailType.EMAIL;
    }

    @Override
    public void verify(Method method, Object[] args, CheckVailCode checkVailCode) {
        if (!vailCodeConfig.isUse()) {
            return;
        }
        // 该项只需要code
        String email = SpElUtils.parseSpEl(method, args, checkVailCode.key());
        String code = SpElUtils.parseSpEl(method, args, checkVailCode.value());

        String zhenvalue = RedisUtils.get(MyString.email_code + email);
        if (StringUtils.isEmpty(zhenvalue)) {
            throw new CustomException("邮箱验证码过期了或者错误，请重新获取");
        }
        if (!zhenvalue.equals(code)) {
            throw new CustomException("邮箱验证码错误，请重新获取");
        }
        RedisUtils.del(MyString.email_code + email);
    }
}
