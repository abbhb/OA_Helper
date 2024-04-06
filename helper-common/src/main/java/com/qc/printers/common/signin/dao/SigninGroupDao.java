package com.qc.printers.common.signin.dao;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qc.printers.common.signin.domain.entity.SigninGroup;
import com.qc.printers.common.signin.mapper.SigninGroupMapper;
import org.springframework.stereotype.Service;

@Service
public class SigninGroupDao extends ServiceImpl<SigninGroupMapper, SigninGroup> {

}
