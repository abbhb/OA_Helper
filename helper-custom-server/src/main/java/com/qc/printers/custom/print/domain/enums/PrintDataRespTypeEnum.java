package com.qc.printers.custom.print.domain.enums;

import com.qc.printers.common.print.domain.vo.response.PrintFileConfigResp;
import com.qc.printers.common.print.domain.vo.response.PrintImageResp;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Description: Print返回给前端数据类型的枚举，比如文件配置和缩略图
 * Date: 2023年10月6日
 */
@AllArgsConstructor
@Getter
public enum PrintDataRespTypeEnum {
    FILECONFIG(1, "打印文件配置resp", PrintFileConfigResp.class),
    IMAGE(2, "缩略图resp", PrintImageResp.class);

    private static Map<Integer, PrintDataRespTypeEnum> cache;

    static {
        cache = Arrays.stream(PrintDataRespTypeEnum.values()).collect(Collectors.toMap(PrintDataRespTypeEnum::getType, Function.identity()));
    }

    private final Integer type;
    private final String desc;
    private final Class dataClass;

    public static PrintDataRespTypeEnum of(Integer type) {
        return cache.get(type);
    }
}
