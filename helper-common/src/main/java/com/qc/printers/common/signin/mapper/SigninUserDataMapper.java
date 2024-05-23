package com.qc.printers.common.signin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qc.printers.common.signin.domain.entity.SigninUserData;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SigninUserDataMapper extends BaseMapper<SigninUserData> {
}
