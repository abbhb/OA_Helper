package com.qc.printers.common.chat.service.strategy.msg;

import com.qc.printers.common.chat.domain.entity.Message;

public interface MsgHandler<RESP, REQ> {

    void saveMsg(Message msg, REQ req);

    RESP showMsg(Message msg);

}
