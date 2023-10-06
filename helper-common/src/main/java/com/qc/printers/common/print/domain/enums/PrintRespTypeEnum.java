package com.qc.printers.common.print.domain.enums;

import com.qc.printers.common.print.domain.vo.response.data.PrintDataFromPDFResp;
import com.qc.printers.common.print.domain.vo.response.data.PrintDataFromPrintResp;
import com.qc.printers.common.print.domain.vo.response.data.PrintDataImageFromPDFResp;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Description: Print操作消费者类型枚举 ：收到消息的类型
 * Date: 2023年10月6日
 */
@AllArgsConstructor
@Getter
public enum PrintRespTypeEnum {
    FROMPDF(1, "转成pdf的返回", PrintDataFromPDFResp.class),
    IMAGEFROMPDF(2, "pdf获取预览图的返回", PrintDataImageFromPDFResp.class),
    PRINTFROMPDF(3, "pdf打印的返回", PrintDataFromPrintResp.class);

    private static Map<Integer, PrintReqTypeEnum> cache;

    static {
        cache = Arrays.stream(PrintReqTypeEnum.values()).collect(Collectors.toMap(PrintReqTypeEnum::getType, Function.identity()));
    }

    private final Integer type;
    private final String desc;
    private final Class dataClass;

    public static PrintReqTypeEnum of(Integer type) {
        return cache.get(type);
    }
}
