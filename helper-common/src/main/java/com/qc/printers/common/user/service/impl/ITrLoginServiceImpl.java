package com.qc.printers.common.user.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qc.printers.common.user.domain.entity.TrLogin;
import com.qc.printers.common.user.mapper.TrLoginMapper;
import com.qc.printers.common.user.service.ITrLoginService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ITrLoginServiceImpl extends ServiceImpl<TrLoginMapper, TrLogin> implements ITrLoginService {

}
