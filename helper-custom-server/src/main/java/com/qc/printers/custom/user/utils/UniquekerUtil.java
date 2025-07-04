package com.qc.printers.custom.user.utils;

import com.qc.printers.common.common.CustomException;
import com.qc.printers.common.common.constant.StringConstant;
import com.qc.printers.custom.user.domain.entity.UniquekerLoginInfo;
import com.qc.printers.custom.user.domain.entity.UniquekerLoginUrl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.client.RestTemplate;

/**
 * 水滴第三方登录工具SDK-JAVA
 * 相关的请求封装在此工具里，前端使用get发送请求，这样能捕获异常！302也正常
 */
@Slf4j
public class UniquekerUtil {
    private static final String APP_ID = "2277";
    private static final String APP_KEY = "c784fcd2eaf6a869f945aedb72db40f2";
    private static final String REDIRECT_URI = "https://"+ StringConstant.YW_YM+"/#/callback";


    /**
     * Get the forward login address
     */
    public static String getForwardLoginUrl(String type) {
        String reqUrl = "https://uniqueker.top/connect.php?act=login&appid=" + APP_ID + "&appkey=" + APP_KEY + "&type=" + type + "&redirect_uri=" + REDIRECT_URI;
        int maxRetries = 3;
        int retry = 0;
        RestTemplate restTemplate = new RestTemplate();
        /**
         * 3次重试
         */
        while (retry < maxRetries) {
            try {
                UniquekerLoginUrl response = restTemplate.getForObject(reqUrl, UniquekerLoginUrl.class);
                // 如果需要处理响应数据，这里进行相应的操作
                assert response != null;
                if (!response.getCode().equals(0)) {
                    throw new CustomException(response.getMsg());
                }
                if (response.getUrl().equals("")) {
                    throw new CustomException("不能为空");
                }
                return response.getUrl();

            } catch (Exception e) {
                // 请求失败，进行重试
                retry++;
            }
        }

        throw new CustomException("Failed to fetch data from external service after " + maxRetries + " retries");
    }

    public static UniquekerLoginInfo getUniquekerLoginInfo(String code, String type) {
        String reqUrl = "https://uniqueker.top/connect.php?act=callback&appid=" + APP_ID + "&appkey=" + APP_KEY + "&type=" + type + "&code=" + code;

        int maxRetries = 3;
        int retry = 0;
        RestTemplate restTemplate = new RestTemplate();
        /**
         * 3次重试
         */
        while (retry < maxRetries) {
            try {
                UniquekerLoginInfo response = restTemplate.getForObject(reqUrl, UniquekerLoginInfo.class);
                log.info("callback-response = {}", response);
                // 如果需要处理响应数据，这里进行相应的操作
                if (response == null) {
                    throw new CustomException("不能为空");
                }
                if (!response.getCode().equals(0)) {
                    throw new CustomException(response.getMsg());
                }
                if (response.getSocialUid() == null || response.getSocialUid().equals("")) {
                    throw new CustomException("不能为空");
                }
                return response;
            } catch (Exception e) {
                // 请求失败，进行重试
                retry++;
            }
        }
        throw new CustomException("Failed to fetch data from external service after " + maxRetries + " retries");

    }

    public static UniquekerLoginInfo getUniquekerLoginInfoBySocialUid(String socialUid, String type) {
        String reqUrl = "https://uniqueker.top/connect.php?act=query&appid=" + APP_ID + "&appkey=" + APP_KEY + "&type=" + type + "&social_uid=" + socialUid;

        RestTemplate restTemplate = new RestTemplate();

        UniquekerLoginInfo response = restTemplate.getForObject(reqUrl, UniquekerLoginInfo.class);
        // 如果需要处理响应数据，这里进行相应的操作
        assert response != null;
        if (!response.getCode().equals(0)) {
            throw new CustomException(response.getMsg());
        }
        if (response.getSocialUid() == null || response.getSocialUid().equals("")) {
            throw new CustomException("不能为空");
        }
        return response;

    }
}
