package com.qc.printers.common.holidays.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qc.printers.common.holidays.domain.Holidays;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface HolidaysMapper extends BaseMapper<Holidays> {
}
