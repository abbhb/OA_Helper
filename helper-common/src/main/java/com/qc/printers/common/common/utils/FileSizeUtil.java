package com.qc.printers.common.common.utils;

import org.springframework.web.multipart.MultipartFile;

public class FileSizeUtil {
    private final static Integer FILE_SIZE = 100;//文件上传限制大小
    private final static String FILE_UNIT = "M";//文件上传限制单位（B,K,M,G）

    /**
     * @param len  文件长度
     * @param size 限制大小
     * @param unit 限制单位（B,K,M,G）
     * @描述 判断文件大小
     */
    public static boolean checkFileSize(Long len, int size, String unit) {
        double fileSize = 0;
        if ("B".equalsIgnoreCase(unit)) {
            fileSize = (double) len;
        } else if ("K".equalsIgnoreCase(unit)) {
            fileSize = (double) len / 1024;
        } else if ("M".equalsIgnoreCase(unit)) {
            fileSize = (double) len / 1048576;
        } else if ("G".equalsIgnoreCase(unit)) {
            fileSize = (double) len / 1073741824;
        }
        return !(fileSize > size);
    }

    //文件上传调用
    public static void check(MultipartFile file) {
        boolean flag = checkFileSize(file.getSize(), FILE_SIZE, FILE_UNIT);
        if (!flag) {
            throw new RuntimeException("上传文件大小超出限制");
        }
    }

}
