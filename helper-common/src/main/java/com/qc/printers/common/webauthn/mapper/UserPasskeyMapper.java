package com.qc.printers.common.webauthn.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qc.printers.common.webauthn.entity.UserPasskey;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserPasskeyMapper extends BaseMapper<UserPasskey> {
}
