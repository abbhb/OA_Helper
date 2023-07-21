package com.qc.printers.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qc.printers.mapper.GroupUserMapper;
import com.qc.printers.pojo.GroupUser;
import com.qc.printers.pojo.vo.GroupAndUserVO;
import com.qc.printers.service.GroupUserService;
import com.qc.printers.utils.RemoveDuplication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
@Transactional
public class GroupUserServiceImpl extends ServiceImpl<GroupUserMapper, GroupUser> implements GroupUserService {

    @Transactional
    @Override
    public void addGroupUser(GroupAndUserVO groupAndUserVO) {
        if (groupAndUserVO.getId() == null) {
            throw new RuntimeException("id不能为空");
        }
        //先去重
        List<GroupUser> list = RemoveDuplication.removeDuplicationByHashSet(groupAndUserVO.getGroupUserList());
        list.forEach(groupUser -> {
            groupUser.setGroupId(groupAndUserVO.getId());
            boolean save1 = this.save(groupUser);
            if (!save1) {
                throw new RuntimeException("保存失败");
            }
        });
    }
}
