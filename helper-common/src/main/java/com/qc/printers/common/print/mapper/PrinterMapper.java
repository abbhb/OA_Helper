package com.qc.printers.common.print.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qc.printers.common.common.annotation.DataScope;
import com.qc.printers.common.print.domain.entity.Printer;
import com.qc.printers.common.print.domain.vo.CountTop10VO;
import com.qc.printers.common.user.domain.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PrinterMapper extends BaseMapper<Printer> {
    @DataScope(userAlias = "user")
    public List<CountTop10VO> getCountTop10(@Param("userT") User userT);

    public List<CountTop10VO> getCountTop10EveryDay();

    public Integer getPrintCount();
}
