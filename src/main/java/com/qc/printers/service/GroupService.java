package com.qc.printers.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.qc.printers.pojo.Group;
import com.qc.printers.pojo.PageData;
import com.qc.printers.pojo.vo.Duplicate;
import com.qc.printers.pojo.vo.GroupAndUserFrontVO;
import com.qc.printers.pojo.vo.GroupAndUserVO;
import com.qc.printers.pojo.vo.GroupTree;

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
