package com.qc.printers.common.contentpromotion.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qc.printers.common.contentpromotion.domain.DocClassification;
import com.qc.printers.common.contentpromotion.mapper.DocClassificationMapper;
import com.qc.printers.common.contentpromotion.service.IDocClassificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class IDocClassificationServiceImpl extends ServiceImpl<DocClassificationMapper, DocClassification> implements IDocClassificationService {
}
