package com.qc.printers.common.signin.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qc.printers.common.signin.domain.entity.SigninLogAskLeave;
import com.qc.printers.common.signin.mapper.SigninLogAskLeaveMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class SigninLogAskLeaveDao extends ServiceImpl<SigninLogAskLeaveMapper,SigninLogAskLeave> {
}
