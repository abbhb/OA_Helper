package com.qc.printers.common.signin.service.impl;

import com.qc.printers.common.signin.dao.SigninLogAskLeaveDao;
import com.qc.printers.common.signin.domain.entity.SigninLogAskLeave;
import com.qc.printers.common.signin.service.SigninLogAskLeaveService;
import io.swagger.v3.oas.annotations.servers.Server;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.Valid;


@Slf4j
@Service
public class SigninLogAskLeaveServiceImpl implements SigninLogAskLeaveService {

    @Autowired
    private SigninLogAskLeaveDao signinLogAskLeaveDao;


    @Transactional
    @Override
    public void addAskLeave(@Valid SigninLogAskLeave signinLogAskLeave) {
        signinLogAskLeaveDao.save(signinLogAskLeave);
    }

    @Transactional
    @Override
    public void delAskLeave(SigninLogAskLeave signinLogAskLeave) {
        signinLogAskLeaveDao.removeById(signinLogAskLeave.getId());
    }
}
