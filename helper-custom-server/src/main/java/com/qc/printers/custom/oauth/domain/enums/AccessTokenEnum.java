package com.qc.printers.custom.oauth.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@AllArgsConstructor
@Getter
public enum AccessTokenEnum {
    ACCESSTOKEN("authorization_code", "获取token"),
    REFRESHTOKEN("refresh_token", "刷新token");

    private static Map<String, AccessTokenEnum> cache;

    static {
        cache = Arrays.stream(AccessTokenEnum.values()).collect(Collectors.toMap(AccessTokenEnum::getGrantType, Function.identity()));
    }

    private final String grantType;

    private final String desc;


    public static AccessTokenEnum of(String grantType) {
        return cache.get(grantType);
    }
}
