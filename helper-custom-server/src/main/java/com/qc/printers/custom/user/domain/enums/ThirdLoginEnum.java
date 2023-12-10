package com.qc.printers.custom.user.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@AllArgsConstructor
@Getter
public enum ThirdLoginEnum {
    QQ("qq", "qq登录"),
    WECHAT("wx", "wx登录");

    private static Map<String, ThirdLoginEnum> cache;

    static {
        cache = Arrays.stream(ThirdLoginEnum.values()).collect(Collectors.toMap(ThirdLoginEnum::getType, Function.identity()));
    }

    private final String type;

    private final String desc;


    public static ThirdLoginEnum of(String type) {
        return cache.get(type);
    }
}
