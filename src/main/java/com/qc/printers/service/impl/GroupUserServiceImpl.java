package com.qc.printers.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qc.printers.mapper.GroupUserMapper;
import com.qc.printers.pojo.GroupUser;
import com.qc.printers.service.GroupUserService;
import org.springframework.stereotype.Service;


@Service
public class GroupUserServiceImpl extends ServiceImpl<GroupUserMapper, GroupUser> implements GroupUserService {
}
