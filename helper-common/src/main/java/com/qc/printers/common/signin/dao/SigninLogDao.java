package com.qc.printers.common.signin.dao;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qc.printers.common.signin.domain.entity.SigninLog;
import com.qc.printers.common.signin.mapper.SigninLogMapper;
import org.springframework.stereotype.Service;

@Service
public class SigninLogDao extends ServiceImpl<SigninLogMapper, SigninLog> {
}
