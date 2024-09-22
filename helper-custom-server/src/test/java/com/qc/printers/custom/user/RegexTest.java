package com.qc.printers.custom.user;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexTest {
    public static final String regex = "https?://([^:/]+)(?::\\d+)?/([^/]+)/([^/?]+)";
//    public static final String regexS = "https?://([^:/]+)(?::\\d+)?/([^/]+)/([^/?]+)";
    public static final String regexS = "https?://([^:/]+)(?::\\\\d+)?/(.*)/(.*)";

    public static void main(String[] args) {
        String url = "http://oss.easyoa.fun/aistuido/12e12/11/22/3/gtest.png";

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

            System.out.println("Host and port: " + host);
            System.out.println("First path part: " + firstPathPart);
            System.out.println("Second path part: " + secondPathPart);
        } else {
            System.out.println("Invalid URL format");
        }

    }
}
