package com.qc.printers.custom.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.qc.printers.common.common.domain.entity.PageData;
import com.qc.printers.custom.user.domain.vo.request.GroupAndUserVO;
import com.qc.printers.custom.user.domain.vo.response.Duplicate;
import com.qc.printers.custom.user.domain.vo.response.GroupAndUserFrontVO;
import com.qc.printers.custom.user.domain.vo.response.GroupTree;

import java.util.List;


public interface GroupService extends IService<Group> {
    void addGroup(GroupAndUserVO groupAndUserVO);

    void deleteGroup(GroupAndUserVO groupAndUserVO);

    void forceDeleteGroup(GroupAndUserVO groupAndUserVO);


    void updateGroup(GroupAndUserVO groupAndUserVO);

    PageData<GroupAndUserFrontVO> queryGroup(Integer pageNum, Integer pageSize, String name);

    Duplicate isDuplicate(String name);

    List<GroupTree> getCanBeAdd(String groupId);

    List<GroupTree> getCanBeAddWFZ(String groupId);
}
