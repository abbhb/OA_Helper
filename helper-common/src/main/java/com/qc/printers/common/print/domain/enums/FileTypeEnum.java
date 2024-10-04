package com.qc.printers.common.print.domain.enums;

import com.qc.printers.common.print.domain.vo.request.data.PrintDataFileToPDFReq;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@AllArgsConstructor
@Getter
public enum FileTypeEnum {
    PDF("pdf", "pdf校验类型",null),
    WORD("word", "word", null),
    EXCEL("excel", "excel", null),
    PPT("ppt", "ppt", null),
    TXT("txt", "txt", null),
    IMAGE("image", "image", null),
    ;

    private static Map<String, FileTypeEnum> cache;

    static {
        cache = Arrays.stream(FileTypeEnum.values()).collect(Collectors.toMap(FileTypeEnum::getType, Function.identity()));
    }

    private final String type;
    private final String desc;
    /**
     * 用于校验的校验器
     */
    private final Class dataClass;

    public static FileTypeEnum of(String houzhui) {
        return switch (houzhui) {
            case "jpg", "bmp", "jpeg", "png" -> cache.get("image");
            case "docx", "doc" -> cache.get("word");
            case "xls", "xlsx" -> cache.get("excel");
            case "pptx", "ppt" -> cache.get("ppt");
            case "pdf" -> cache.get("pdf");
            case "txt" -> cache.get("txt");
            default -> null;
        };

    }
}
