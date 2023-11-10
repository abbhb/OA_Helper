package com.qc.printers.common.vailcode.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@AllArgsConstructor
@Getter
public enum VailType {
    VAILCODE(0, "滑动验证码"),
    EMAIL(1, "邮箱验证码");

    private static Map<Integer, VailType> cache;

    static {
        cache = Arrays.stream(VailType.values()).collect(Collectors.toMap(VailType::getType, Function.identity()));
    }

    private final Integer type;

    private final String desc;


    public static VailType of(Integer type) {
        return cache.get(type);
    }
}
