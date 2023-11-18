package com.qc.printers.common.common.utils;

import java.time.Instant;
import java.util.UUID;

public class RandomName {
    public static String getRandomName(String fileName) {
        int index = fileName.lastIndexOf(".");
        String houzhui = fileName.substring(index);//获取后缀名
        String fileNameWithoutSuffix = fileName.substring(0, index - 1);
        String uuidFileName = fileNameWithoutSuffix + Instant.now().toEpochMilli() + houzhui;
        return uuidFileName;
    }

    public static String getUUID(){
        UUID uuid=UUID.randomUUID();
        String str = uuid.toString();
        String uuidStr=str.replace("-", "");
        return uuidStr;
    }
}
