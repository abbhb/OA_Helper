package com.qc.printers.custom.notice.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@AllArgsConstructor
@Getter
public enum NoticeStatusEnum {
    DRAFT(0, "草稿"),
    PRERELEASE(1, "预发布"),
    PUBLISH(2, "正式发布/定时发布[定时发布唯一区别为指定了可见时间，正式发布就是时间默认]"),
    BANVIEW(3, "禁止查看"),
    ;

    private static Map<Integer, NoticeStatusEnum> cache;

    static {
        cache = Arrays.stream(NoticeStatusEnum.values()).collect(Collectors.toMap(NoticeStatusEnum::getStatus, Function.identity()));
    }

    private final Integer status;

    private final String desc;

    public static NoticeStatusEnum of(Integer status) {
        return cache.get(status);
    }
}
