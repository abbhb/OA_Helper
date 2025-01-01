package com.qc.printers.common.print.dao;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qc.printers.common.print.domain.entity.SysPrintDevice;
import com.qc.printers.common.print.mapper.SysPrintDeviceMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class SysPrintDeviceDao extends ServiceImpl<SysPrintDeviceMapper, SysPrintDevice> {
}
