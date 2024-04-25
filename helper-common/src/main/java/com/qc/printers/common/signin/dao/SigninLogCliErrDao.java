package com.qc.printers.common.signin.dao;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qc.printers.common.signin.domain.entity.SigninLogCliErr;
import com.qc.printers.common.signin.mapper.SigninLogCliErrMapper;
import org.springframework.stereotype.Service;

@Service
public class SigninLogCliErrDao extends ServiceImpl<SigninLogCliErrMapper, SigninLogCliErr> {
}
