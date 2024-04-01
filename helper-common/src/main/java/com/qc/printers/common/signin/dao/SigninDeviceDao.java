package com.qc.printers.common.signin.dao;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qc.printers.common.signin.domain.entity.SigninDevice;
import com.qc.printers.common.signin.mapper.SigninDeviceMapper;
import org.springframework.stereotype.Service;

@Service
public class SigninDeviceDao extends ServiceImpl<SigninDeviceMapper, SigninDevice> {
}
