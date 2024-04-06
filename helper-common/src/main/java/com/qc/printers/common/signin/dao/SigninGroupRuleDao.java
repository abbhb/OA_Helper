package com.qc.printers.common.signin.dao;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qc.printers.common.signin.domain.entity.SigninGroupRule;
import com.qc.printers.common.signin.mapper.SigninGroupRuleMapper;
import org.springframework.stereotype.Service;

@Service
public class SigninGroupRuleDao extends ServiceImpl<SigninGroupRuleMapper, SigninGroupRule> {
}
