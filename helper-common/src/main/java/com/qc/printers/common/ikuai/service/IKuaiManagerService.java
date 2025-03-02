package com.qc.printers.common.ikuai.service;

import com.qc.printers.common.common.CustomException;
import com.qc.printers.common.common.constant.StringConstant;
import com.qc.printers.common.common.domain.entity.CommonConfig;
import com.qc.printers.common.common.service.CommonConfigService;
import com.qc.printers.common.common.utils.RedisUtils;
import com.qc.printers.common.common.utils.StringUtils;
import com.qc.printers.common.config.IKuaiConfig;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
public class IKuaiManagerService {
    private static final String SESS_KEY_PREFIX = "ikuai:sess:";
    private static final Duration TTL = Duration.ofMinutes(60); // 60分钟有效期

    private final RestTemplate restTemplate;

    @Autowired
    private IKuaiConfig iKuaiConfig;

    @Autowired
    private CommonConfigService commonConfigService;

    @Autowired
    public IKuaiManagerService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    // todo:此处需要写在配置中心里，不然藏得太隐蔽
    /**
     * 获取 SessKey（优先从缓存读取）
     */
    public String getSessKey() {
        CommonConfig usernameO = commonConfigService.getById(StringConstant.IKUAI_USER);
        if (usernameO==null){
            throw new CustomException("ikuai 用户名未设置");
        }
        CommonConfig passwordO = commonConfigService.getById(StringConstant.IKUAI_PASSWORD);
        if (passwordO==null){
            throw new CustomException("ikuai 密码未设置");
        }
        String key = SESS_KEY_PREFIX + usernameO.getConfigValue();
        String sessKey = RedisUtils.get(key);
        if (StringUtils.isEmpty(sessKey)) {
            sessKey = refreshSessKey(usernameO.getConfigValue(), passwordO.getConfigValue());
        }
        return sessKey;
    }

    /**
     * 调用登录接口并缓存 SessKey
     */
    public String refreshSessKey(String username, String password) {
        // 调用 iKuai 登录接口（示例代码）
        Map<String, String> params = new HashMap<>();
        params.put("username", username);
        params.put("passwd", DigestUtils.md5Hex(password));
        String pass = "salt_11"+password;
        String encodedString = Base64.getEncoder().encodeToString(pass.getBytes());
        params.put("pass", encodedString);
        params.put("remember_password", "");

        ResponseEntity<Map> response = restTemplate.postForEntity(
                "http://"+iKuaiConfig.getIp()+":"+iKuaiConfig.getPort()+"/Action/login", params, Map.class);

        if (response.getBody().get("Result").equals(10000)) {
            String sessKey = extractSessKeyFromCookies(response.getHeaders());
            RedisUtils.set(SESS_KEY_PREFIX + username, sessKey, TTL.getSeconds(),TimeUnit.SECONDS);
            return sessKey;
        } else {
            throw new CustomException("登录失败");
        }
    }

    /**
     * 从 HTTP 响应头中提取 sess_key
     * @param headers 响应头对象
     * @return sess_key 字符串
     */
    private String extractSessKeyFromCookies(HttpHeaders headers) {
        List<String> cookieHeaders = headers.get(HttpHeaders.SET_COOKIE);

        if (cookieHeaders == null || cookieHeaders.isEmpty()) {
            throw new CustomException("登录响应中未找到 Cookie 信息");
        }

        return cookieHeaders.stream()
                .map(cookie -> cookie.split(";")[0].trim()) // 取第一个键值对（sess_key=xxx）
                .filter(kv -> kv.startsWith("sess_key="))
                .map(kv -> kv.split("=", 2)[1]) // 分割值部分
                .findFirst()
                .orElseThrow(() -> new CustomException("Cookie 中未找到 sess_key"));
    }

}
