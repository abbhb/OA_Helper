package com.qc.printers.custom.print.domain.enums;

import com.qc.printers.custom.print.service.strategy.fileconfig.*;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Description: print在redis里的stu状态码
 * Date: 2023年10月6日
 */
@AllArgsConstructor
@Getter
public enum PrintFileConfigStuEnum {
    ERROR(0, "出现错误，携带message", FileConfigErrorType.class),
    USERFINISHUPLOAD(1, "用户刚上传完文件", FileConfigUserFinishUploadType.class),
    STARTTOPDF(2, "开始转pdf", FileConfigStartToPdfType.class),
    PDFFINISH(3, "转pdf完成", FileConfigPdfFinishType.class),
    STARTPRINT(4, "开始打印", FileConfigStartPrintType.class),
    PRINTFINISH(5, "打印完成", FileConfigPrintFinishType.class);

    private static Map<Integer, PrintFileConfigStuEnum> cache;

    static {
        cache = Arrays.stream(PrintFileConfigStuEnum.values()).collect(Collectors.toMap(PrintFileConfigStuEnum::getType, Function.identity()));
    }

    private final Integer type;
    private final String desc;
    private final Class dataClass;

    public static PrintFileConfigStuEnum of(Integer type) {
        return cache.get(type);
    }
}
