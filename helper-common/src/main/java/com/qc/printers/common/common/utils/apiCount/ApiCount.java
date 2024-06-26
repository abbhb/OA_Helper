package com.qc.printers.common.common.utils.apiCount;

import com.qc.printers.common.common.MyString;
import com.qc.printers.common.common.utils.RedisUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * 本系统请求量
 */
@Slf4j
public class ApiCount {

    /**
     * 清空
     */
    public static void cleanApiCount() {
        RedisUtils.set(MyString.pre_api_count_latday, (Integer) RedisUtils.get(MyString.pre_api_count, Integer.class));
        RedisUtils.set(MyString.pre_api_count, 0);
    }


    /**
     * 递增
     */
    public static void addApiCount() {
        RedisUtils.increment(MyString.pre_api_count);
    }

    public static Integer getApiCount() {
        return (Integer) RedisUtils.get(MyString.pre_api_count, Integer.class);
    }

    public static Integer getLastDayCountApi() {
        Object o = RedisUtils.get(MyString.pre_api_count_latday, Integer.class);
        if (o == null) {
            return 0;
        }
        return (int) o;
    }
}
