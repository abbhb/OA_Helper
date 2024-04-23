package com.qc.printers.common.signin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qc.printers.common.common.CustomException;
import com.qc.printers.common.common.MyString;
import com.qc.printers.common.signin.dao.SigninLogDao;
import com.qc.printers.common.signin.domain.entity.SigninLog;
import com.qc.printers.common.signin.service.SigninDeviceMangerService;
import com.qc.printers.common.signin.service.SigninLogService;
import com.qc.printers.common.user.dao.UserDao;
import com.qc.printers.common.user.domain.entity.User;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;

@Service
public class SigninLogServiceImpl implements SigninLogService {
    @Autowired
    private SigninLogDao signinLogDao;

    @Autowired
    private UserDao userDao;
    @Autowired
    private SigninDeviceMangerService signinDeviceMangerService;

    @Transactional
    public void addSigninlog(SigninLog signinLog) {
        if (signinLog.getSigninWay().equals("face")) {
            if (StringUtils.isEmpty(signinLog.getSigninImage())) throw new CustomException("请上传原始签到图");

        }
        if (!signinLog.getSigninWay().equals("system")) {
            if (StringUtils.isEmpty(signinLog.getSigninDeviceId())) throw new CustomException("必须提供设备id");
        }
        signinLogDao.save(signinLog);
    }

    /**
     * signinImage为base64
     *
     * @param request
     * @param signinLog
     * @return
     */
    @Transactional
    @Override
    public String addSigninlogByDevice(HttpServletRequest request, SigninLog signinLog) {
        final String signinSecret = request.getHeader(MyString.SIGNIN_DEVICE_HEADER_KEY);
        if (!signinDeviceMangerService.checkDevice(signinLog.getSigninDeviceId(), signinSecret))
            throw new CustomException("鉴权失败");
        LambdaQueryWrapper<User> userLambdaQueryWrapper = new LambdaQueryWrapper<>();
        userLambdaQueryWrapper.eq(User::getStudentId, signinLog.getStudentId());
        User one = userDao.getOne(userLambdaQueryWrapper);
        if (one == null) {
            throw new CustomException("该学号暂未绑定用户!");
        }
        signinLog.setUserId(one.getId());
        this.addSigninlog(signinLog);
        return "记录成功";
    }
}
