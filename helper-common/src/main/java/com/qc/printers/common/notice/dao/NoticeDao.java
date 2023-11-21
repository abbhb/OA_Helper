package com.qc.printers.common.notice.dao;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qc.printers.common.notice.domain.entity.Notice;
import com.qc.printers.common.notice.mapper.NoticeMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class NoticeDao extends ServiceImpl<NoticeMapper, Notice> {

}
