package com.qc.printers.common.confirm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qc.printers.common.confirm.dao.SysConfirmDao;
import com.qc.printers.common.confirm.domain.entity.SysConfirm;
import com.qc.printers.common.confirm.service.SysConfirmService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class SysConfirmServiceImpl implements SysConfirmService {
    @Autowired
    private SysConfirmDao sysConfirmDao;


    @Transactional
    @Override
    public void confirm(String key, Long userId) {
        if (isConfirmed(key, userId)) {
            // 已经确认过
            return;
        }
        SysConfirm sysConfirm = new SysConfirm();
        sysConfirm.setKey(key);
        sysConfirm.setUserId(userId);
        sysConfirmDao.save(sysConfirm);
    }

    @Override
    public boolean isConfirmed(String key, Long userId) {
        LambdaQueryWrapper<SysConfirm> sysConfirmLambdaQueryWrapper = new LambdaQueryWrapper<>();
        sysConfirmLambdaQueryWrapper.eq(SysConfirm::getKey, key);
        sysConfirmLambdaQueryWrapper.eq(SysConfirm::getUserId, userId);
        if (sysConfirmDao.count(sysConfirmLambdaQueryWrapper) > 0) {
            return true;
        }
        return false;
    }

    @Override
    public Integer getConfirmCount(String key) {
        LambdaQueryWrapper<SysConfirm> sysConfirmLambdaQueryWrapper = new LambdaQueryWrapper<>();
        sysConfirmLambdaQueryWrapper.eq(SysConfirm::getKey, key);
        return sysConfirmDao.count(sysConfirmLambdaQueryWrapper);
    }
}
