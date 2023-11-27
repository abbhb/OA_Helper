package com.qc.printers.common.confirm.service;

public interface SysConfirmService {

    void confirm(String key, Long userId);

    boolean isConfirmed(String key, Long userId);

    /**
     * 某个key多少人已经确认过
     *
     * @param key
     * @return
     */
    Integer getConfirmCount(String key);
}
