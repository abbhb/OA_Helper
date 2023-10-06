package com.qc.printers.common.print.domain.enums;


import com.qc.printers.common.print.domain.vo.request.data.PrintDataFileToPDFReq;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Description: Print操作生产者类型枚举 ：发送消息的类型
 * 方便后续升级兼容老版本
 * Date: 2023年10月6日
 */
@AllArgsConstructor
@Getter
public enum PrintReqTypeEnum {
    TOPDF(1, "非pdf转pdf", PrintDataFileToPDFReq.class),
    PDFGETIMAGE(2, "pdf获取pdf预览图", PrintDataFileToPDFReq.class),
    PDFTOPRINT(3, "pdf加入打印队列等待打印", PrintDataFileToPDFReq.class),

    ;

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
