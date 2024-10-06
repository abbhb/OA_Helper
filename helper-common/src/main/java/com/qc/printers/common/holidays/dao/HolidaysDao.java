package com.qc.printers.common.holidays.dao;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qc.printers.common.holidays.domain.Holidays;
import com.qc.printers.common.holidays.mapper.HolidaysMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class HolidaysDao extends ServiceImpl<HolidaysMapper, Holidays> {

}
