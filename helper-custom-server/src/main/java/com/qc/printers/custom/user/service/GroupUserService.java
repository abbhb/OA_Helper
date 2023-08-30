package com.qc.printers.custom.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.qc.printers.custom.user.domain.vo.request.GroupAndUserVO;


public interface GroupUserService extends IService<GroupUser> {
    void addGroupUser(GroupAndUserVO groupAndUserVO);

}
