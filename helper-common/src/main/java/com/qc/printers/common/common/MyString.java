package com.qc.printers.common.common;

public class MyString {
    public final static String pre = "AIEN::";

    public final static String study_clock = pre + "CLOCK::";
    /**
     * 缓存前缀
     */
    public final static String cache = pre + "CACHE::";
    public final static String pre_api_count = pre + "today:" + "apiCount";
    public final static String pre_api_count_latday = pre + "lastday:" + "apiCount";

    public final static String public_file = "D:\\printFile";

    public final static String permission_key = "permission";

    public final static String print_document_type_statistic = cache + "PrintDocumentTypeStatistic";

    public final static String pre_common_config = cache + "common_config";
}