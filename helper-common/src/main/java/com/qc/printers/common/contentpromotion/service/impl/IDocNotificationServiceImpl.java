package com.qc.printers.common.contentpromotion.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qc.printers.common.contentpromotion.domain.DocNotification;
import com.qc.printers.common.contentpromotion.mapper.DocNotificationMapper;
import com.qc.printers.common.contentpromotion.service.IDocNotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class IDocNotificationServiceImpl extends ServiceImpl<DocNotificationMapper, DocNotification> implements IDocNotificationService {
}
