package com.qc.printers.common.signin.dao;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qc.printers.common.signin.domain.entity.SigninUserData;
import com.qc.printers.common.signin.mapper.SigninUserDataMapper;
import org.springframework.stereotype.Service;

@Service
public class SigninUserDataDao extends ServiceImpl<SigninUserDataMapper, SigninUserData> {

}
