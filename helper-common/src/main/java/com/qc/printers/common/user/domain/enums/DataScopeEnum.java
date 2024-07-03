package com.qc.printers.common.user.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@AllArgsConstructor
@Getter
public enum DataScopeEnum {
    ALL_DATA_SCOPE(1, "所有数据可见","拥有这个数据权限能看到所有的数据"),
    CURRENT_DEPT_DATA_SCOPE(2, "仅本部门及下级部门数据可见","能看到当前用户的部门的数据，但是优先级小于1，如果所有数据可见拥有的话，此项就失效"),
    CUSTOM_DATA_SCOPE(3, "自定义权限","能手动选择拥有哪些部门可见，和2叠加，使4失效，1能使该项失效"),
    ONLY_CURRENT_USER_DATA_SCOPE(4, "仅本人","优先级最小，如果上面拥有此项就失效"),
    ;

    private static Map<Integer, DataScopeEnum> cache;

    static {
        cache = Arrays.stream(DataScopeEnum.values()).collect(Collectors.toMap(DataScopeEnum::getType, Function.identity()));
    }

    private final Integer type;

    private final String name;
    private final String desc;

    public static DataScopeEnum of(Integer type) {
        return cache.get(type);
    }
    public static String name(Integer type) {
        return cache.get(type).getName();
    }
}
