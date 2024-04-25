package com.qc.printers.common.signin.dao;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qc.printers.common.signin.domain.entity.SigninLogCli;
import com.qc.printers.common.signin.mapper.SigninLogCliMapper;
import org.springframework.stereotype.Service;

@Service
public class SigninLogCliDao extends ServiceImpl<SigninLogCliMapper, SigninLogCli> {
}
