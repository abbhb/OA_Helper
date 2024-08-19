package com.qc.printers.common.chat.dao;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qc.printers.common.chat.domain.entity.MessageUserState;
import com.qc.printers.common.chat.mapper.MessageUserStateMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 消息_用户关系
 */
@Service
@Slf4j
public class MessageUserStateDao extends ServiceImpl<MessageUserStateMapper, MessageUserState> {
}
