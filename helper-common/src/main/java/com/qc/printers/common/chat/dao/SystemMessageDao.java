package com.qc.printers.common.chat.dao;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qc.printers.common.chat.domain.entity.SystemMessage;
import com.qc.printers.common.chat.mapper.SystemMessageMapper;
import org.springframework.stereotype.Service;

/**
 * 系统消息，在发送的时候存入这个库
 */
@Service
public class SystemMessageDao extends ServiceImpl<SystemMessageMapper, SystemMessage> {
}
