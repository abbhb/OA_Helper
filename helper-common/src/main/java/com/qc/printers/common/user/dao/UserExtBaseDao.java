package com.qc.printers.common.user.dao;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qc.printers.common.user.domain.entity.UserExtBase;
import com.qc.printers.common.user.mapper.UserExtBaseMapper;
import org.springframework.stereotype.Service;

@Service
public class UserExtBaseDao extends ServiceImpl<UserExtBaseMapper, UserExtBase> {
}
