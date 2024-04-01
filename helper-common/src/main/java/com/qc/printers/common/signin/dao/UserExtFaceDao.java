package com.qc.printers.common.signin.dao;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qc.printers.common.signin.domain.entity.UserExtFace;
import com.qc.printers.common.signin.mapper.UserExtFaceMapper;
import org.springframework.stereotype.Service;

@Service
public class UserExtFaceDao extends ServiceImpl<UserExtFaceMapper, UserExtFace> {
}
