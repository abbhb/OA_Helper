package com.qc.printers.common.common.utils;

import com.qc.printers.common.common.CustomException;
import com.qc.printers.common.common.MyString;
import com.qc.printers.common.user.domain.entity.User;
import org.apache.commons.lang.StringUtils;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class OneTimeSetPasswordCodeUtil {
    public static String createOneTimeSetPasswordCode(User user) {
        if (user == null) {
            throw new CustomException("用户异常");
        }
        if (StringUtils.isNotEmpty(user.getPassword())) {
            // 该用户已经存在密码了，不需要在设置了
            return null;
        }
        String oneTimeSetPasswordCode = UUID.randomUUID().toString().replaceAll("-", "");
        RedisUtils.set(MyString.one_time_code_key + oneTimeSetPasswordCode, user, 300L, TimeUnit.SECONDS);
        return oneTimeSetPasswordCode;
    }
}
