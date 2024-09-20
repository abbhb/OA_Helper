package com.qc.printers.common.common.utils.oss;

import com.qc.printers.common.common.CustomException;
import com.qc.printers.common.config.MinioStaticConfiguration;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OssDBUtil {

    public static final String regex = "http?://([^:/]+)(?::\\d+)?/([^/]+)/([^/?]+)";
    public static final String regexS = "https?://([^:/]+)(?::\\d+)?/([^/]+)/([^/?]+)";

    public static String toDBUrl(String url) {
        if (StringUtils.isEmpty(url)) {
            return url;
        }
        Pattern pattern = Pattern.compile(regex);


        Matcher matcher = pattern.matcher(url);
        // https优先级必须更高，否则报错
        if (url.startsWith("https")) {
            Pattern patternS = Pattern.compile(regexS);
            Matcher matcherS = patternS.matcher(url);
            // 提取匹配的结果
            List<String[]> resultList = new ArrayList<>();
            while (matcher.find()) {
                String domain = matcher.group(1);
                String bucketName = matcher.group(2);
                String resourceName = matcher.group(3);
                String[] result = {domain, bucketName, resourceName};
                resultList.add(result);
            }

//        // 打印结果
//        for (String[] result : resultList) {
//            System.out.println("Domain: " + result[0]);
//            System.out.println("Bucket Name: " + result[1]);
//            System.out.println("Resource Name: " + result[2]);
//        }
            if (resultList.size() < 1) {
                throw new CustomException("业务异常");
            }

            return resultList.get(0)[1] + "/" + resultList.get(0)[2];
        }

        if (url.startsWith("http")) {
            Pattern patternS = Pattern.compile(regex);
            Matcher matcherS = patternS.matcher(url);
            // 提取匹配的结果
            List<String[]> resultList = new ArrayList<>();
            while (matcher.find()) {
                String domain = matcher.group(1);
                String bucketName = matcher.group(2);
                String resourceName = matcher.group(3);
                String[] result = {domain, bucketName, resourceName};
                resultList.add(result);
            }

//        // 打印结果
//        for (String[] result : resultList) {
//            System.out.println("Domain: " + result[0]);
//            System.out.println("Bucket Name: " + result[1]);
//            System.out.println("Resource Name: " + result[2]);
//        }
            if (resultList.size() < 1) {
                throw new CustomException("业务异常");
            }

            return resultList.get(0)[1] + "/" + resultList.get(0)[2];
        }


        return url;
    }

    public static String toUseUrl(String url){
        if (StringUtils.isEmpty(url)){
            return url;
        }
        if (url.startsWith("http")){
            return url;
        }
        return MinioStaticConfiguration.MINIO_URL + "/" + url;
    }



}
