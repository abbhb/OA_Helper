package com.qc.printers.common.user.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Description: 场景枚举
 * Author: <a href="https://github.com/zongzibinbin">abin</a>
 * Date: 2023-06-20
 */
@AllArgsConstructor
@Getter
public enum OssSceneEnum {
    CHAT(1, "聊天", "/chat"),
    EMOJI(2, "表情包", "/emoji"),
    ;

    private static final Map<Integer, OssSceneEnum> cache;

    static {
        cache = Arrays.stream(OssSceneEnum.values()).collect(Collectors.toMap(OssSceneEnum::getType, Function.identity()));
    }

    private final Integer type;
    private final String desc;
    private final String path;

    public static OssSceneEnum of(Integer type) {
        return cache.get(type);
    }
}
