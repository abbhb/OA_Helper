package com.qc.printers.common.oauth.utils;

import com.qc.printers.common.common.utils.RandomName;

public class OauthUtil {
    public static String genCode() {
        //生成base64编码的40位随机字符串不包含符号
        String code = RandomName.getUUID();
        return code;
    }
}
