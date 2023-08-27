package com.qc.printers.common.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qc.printers.common.common.CustomException;
import com.qc.printers.common.user.domain.entity.User;
import com.qc.printers.common.user.mapper.UserMapper;
import com.qc.printers.common.user.service.IUserService;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class IUserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {
    //重写save方法,校验用户名重复
    //不使用唯一索引是为了逻辑删除后避免用户名不能再次使用
    @Transactional
    @Override
    public boolean save(User entity) {
        if (StringUtils.isEmpty(entity.getUsername())) {
            throw new CustomException("err:user:save");
        }
        LambdaQueryWrapper<User> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(User::getUsername, entity.getUsername());
        //会自动加上条件判断没有删除
        int count = super.count(lambdaQueryWrapper);
        if (count > 0) {
            throw new CustomException("用户名已经存在");
        }
        return super.save(entity);
    }

}
