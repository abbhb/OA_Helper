package com.qc.printers.common.signin.dao;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qc.printers.common.signin.domain.entity.SigninBc;
import com.qc.printers.common.signin.mapper.SigninBcMapper;
import org.springframework.stereotype.Service;

@Service
public class SigninBcDao extends ServiceImpl<SigninBcMapper, SigninBc> {
}
