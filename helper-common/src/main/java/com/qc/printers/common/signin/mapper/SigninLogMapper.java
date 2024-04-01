package com.qc.printers.common.signin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qc.printers.common.signin.domain.entity.SigninLog;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SigninLogMapper extends BaseMapper<SigninLog> {
}
