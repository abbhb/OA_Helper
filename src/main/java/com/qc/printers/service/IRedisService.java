package com.qc.printers.service;

import com.qc.printers.pojo.PrintDocumentTypeStatistic;

import java.util.List;

public interface IRedisService {
//    String getTokenId(String token);

    Object get(String key);

    void set(String key, Object Object);

    void setTokenWithTime(String token, String value, Long time);

    void del(String token);

    Long getTokenTTL(String uuid);

    String getValue(String key);

    void hashPut(String key, String hashKey, Object object);

    Object getHash(String key, String hashKey);

    void addApiCount();

    void cleanApiCount();

    int getLastDayCountApi();

    int getCountApi();

    /**
     * 打印排行榜相关的缓存
     */

    /**
     * 获取打印类别统计的数据缓存
     *
     * @return 打印类别统计的数据缓存
     */
    List<PrintDocumentTypeStatistic> getPrintDocumentTypeStatistics();

    /**
     * 设置打印类别统计的数据缓存
     */
    void setPrintDocumentTypeStatistics(List<PrintDocumentTypeStatistic> printDocumentTypeStatisticList);
}
