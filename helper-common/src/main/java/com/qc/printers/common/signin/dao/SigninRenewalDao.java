package com.qc.printers.common.signin.dao;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qc.printers.common.signin.domain.entity.SigninRenewal;
import com.qc.printers.common.signin.mapper.SigninRenewalMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@Slf4j
public class SigninRenewalDao extends ServiceImpl<SigninRenewalMapper, SigninRenewal> {

}
