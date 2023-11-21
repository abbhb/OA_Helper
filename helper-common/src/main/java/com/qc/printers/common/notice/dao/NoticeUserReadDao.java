package com.qc.printers.common.notice.dao;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qc.printers.common.notice.domain.entity.NoticeUserRead;
import com.qc.printers.common.notice.mapper.NoticeUserReadMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class NoticeUserReadDao extends ServiceImpl<NoticeUserReadMapper, NoticeUserRead> {

}
