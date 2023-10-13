package com.qc.printers.custom.print.domain.enums;

import com.qc.printers.custom.print.service.strategy.image.*;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Description: print在redis里的stu状态码
 * for:Image
 * Date: 2023年10月6日
 */
@AllArgsConstructor
@Getter
public enum PrintImageStuEnum {
    ERROR(0, "出现错误，携带message", ImageErrorType.class),
    USERFINISHUPLOAD(1, "用户刚上传完文件", ImageUserFinishUploadType.class),
    STARTTOPDF(2, "开始转pdf", ImageStartToPdfType.class),
    PDFFINISH(3, "转pdf完成", ImagePdfFinishType.class),
    STARTPRINT(4, "开始打印", ImageStartPrintType.class),
    PRINTFINISH(5, "打印完成", ImagePrintFinishType.class);

    private static Map<Integer, PrintImageStuEnum> cache;

    static {
        cache = Arrays.stream(PrintImageStuEnum.values()).collect(Collectors.toMap(PrintImageStuEnum::getType, Function.identity()));
    }

    private final Integer type;
    private final String desc;
    private final Class dataClass;

    public static PrintImageStuEnum of(Integer type) {
        return cache.get(type);
    }
}
