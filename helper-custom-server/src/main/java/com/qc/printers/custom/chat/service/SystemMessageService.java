package com.qc.printers.custom.chat.service;

import com.qc.printers.common.chat.domain.vo.response.SystemMessageResp;

import java.util.List;

public interface SystemMessageService {
    List<SystemMessageResp> list();

    Integer noreadCount();

    String read(Long id,Integer type);

    String readBatch(List<Long> ids, Integer type);
}
