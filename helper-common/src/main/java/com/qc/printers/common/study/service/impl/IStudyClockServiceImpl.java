package com.qc.printers.common.study.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qc.printers.common.study.domain.entity.StudyClock;
import com.qc.printers.common.study.mapper.StudyClockMapper;
import com.qc.printers.common.study.service.IStudyClockService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class IStudyClockServiceImpl extends ServiceImpl<StudyClockMapper, StudyClock> implements IStudyClockService {
}
