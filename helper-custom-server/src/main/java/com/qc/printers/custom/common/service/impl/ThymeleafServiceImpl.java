package com.qc.printers.custom.common.service.impl;

import com.qc.printers.common.common.MyString;
import com.qc.printers.common.common.utils.RedisUtils;
import com.qc.printers.common.user.dao.UserDao;
import com.qc.printers.common.user.domain.dto.UserInfo;
import com.qc.printers.common.user.domain.entity.User;
import com.qc.printers.custom.common.service.ThymeleafService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

@Service
@Slf4j
public class ThymeleafServiceImpl implements ThymeleafService {

    @Autowired
    private UserDao userDao;

    @Override
    public String reNewPassword(String oneTimeCode, Model model) {
        if (StringUtils.isEmpty(oneTimeCode)) {
            model.addAttribute("error", "一次性身份验证码错误");
            return "error";
        }
        log.info("re-new-password");
        UserInfo userInfo = RedisUtils.get(MyString.one_time_code_key + oneTimeCode, UserInfo.class);
        if (userInfo == null) {
            model.addAttribute("error", "一次性身份验证码已过期或者已经使用！");
            return "error";
        }
        User byId = userDao.getById(userInfo.getId());
        if (byId == null) {
            model.addAttribute("error", "用户不存在");
            return "error";
        }
        model.addAttribute("userName", byId.getName());
        model.addAttribute("oneTimeCode", oneTimeCode);
        return "reNewPassword";
    }
}
