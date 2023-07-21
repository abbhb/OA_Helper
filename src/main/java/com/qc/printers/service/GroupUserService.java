package com.qc.printers.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.qc.printers.pojo.GroupUser;
import com.qc.printers.pojo.vo.GroupAndUserVO;

public interface GroupUserService extends IService<GroupUser> {
    void addGroupUser(GroupAndUserVO groupAndUserVO);

}
