package com.qc.printers.common.common.utils.oss;

import com.qc.printers.common.config.MinioStaticConfiguration;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OssDBUtil {

    public static final String httpRegEx = "/^http:\\/\\/[^\\/]+\\//i";
    public static final String httpsRegEx = "/^https:\\/\\/[^\\/]+\\//i";
    public static String toDBUrl(String url){
        Pattern pattern = Pattern.compile(httpRegEx);
        Matcher matcher = pattern.matcher(url);
        String trim = matcher.replaceAll("").trim();
        Pattern patterns = Pattern.compile(httpsRegEx);
        Matcher matchers = patterns.matcher(trim);
        return matchers.replaceAll("").trim();

    }

    public static String toUseUrl(String url){
        if (url.startsWith("http")){
            return url;
        }
        return MinioStaticConfiguration.MINIO_URL + "/" + url;
    }



}
