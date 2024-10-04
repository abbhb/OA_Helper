package com.qc.printers.common.common.utils.oss;

import com.qc.printers.common.common.CustomException;
import com.qc.printers.common.config.MinioStaticConfiguration;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OssDBUtil {

    public static final String regexS = "https?://([^:/]+)(?::\\d+)?/([^/]+)/([^/?]+)";

    public static String toDBUrl(String url) {
        if (StringUtils.isEmpty(url)) {
            return url;
        }
        String[] split = url.split("\\?");
        url = split[0];// 如果有签名信息除掉，入库数据无需携带签名

        if (!url.startsWith("http")) {
            return url;
        }

        // 正则表达式匹配主机名和路径
        Pattern pattern = Pattern.compile("^(https?://[^/]+)(.*)$");
        Matcher matcher = pattern.matcher(url);

        if (matcher.find()) {
            String host = matcher.group(1); // easyoa.fun:9090
            String path = matcher.group(2); // /aistuido/12e12/xxx.png/241/fvewgw

            // 分割路径以获取单独的部分
            String[] pathParts = path.split("/");
            String firstPathPart = pathParts[1]; // aistuido
            String secondPathPart = ""; // 初始化为空字符串

            if (pathParts.length >= 3) {
                // 从第四个元素开始，将剩余的部分合并为文件名
                for (int i = 2; i < pathParts.length; i++) {
                    if (i > 2) {
                        secondPathPart += "/"; // 添加斜杠
                    }
                    secondPathPart += pathParts[i];
                }
            }

            return firstPathPart + "/" + secondPathPart;

        }

        throw new CustomException("异常的url-3800500");
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
