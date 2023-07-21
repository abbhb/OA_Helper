package com.qc.printers.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qc.printers.common.CustomException;
import com.qc.printers.mapper.GroupMapper;
import com.qc.printers.pojo.Group;
import com.qc.printers.pojo.GroupUser;
import com.qc.printers.pojo.PageData;
import com.qc.printers.pojo.User;
import com.qc.printers.pojo.vo.*;
import com.qc.printers.service.GroupService;
import com.qc.printers.service.GroupUserService;
import com.qc.printers.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Slf4j
public class GroupServiceImpl extends ServiceImpl<GroupMapper, Group> implements GroupService {
    private final GroupUserService groupUserService;
    private final UserService userService;

    public GroupServiceImpl(GroupUserService groupUserService, UserService userService) {
        this.groupUserService = groupUserService;
        this.userService = userService;
    }

    @Transactional
    @Override
    public void addGroup(GroupAndUserVO groupAndUserVO) {
        if (groupAndUserVO == null || groupAndUserVO.getGroupUserList() == null) {
            throw new IllegalArgumentException("参数不合法");
        }
        if (this.isDuplicate(groupAndUserVO.getName()).isDuplicate()) {
            throw new CustomException("组名已存在");
        }
        Group group = new Group();
        group.setName(groupAndUserVO.getName());
        boolean save = this.save(group);
        if (!save) {
            throw new RuntimeException("保存失败");
        }
        groupAndUserVO.setId(group.getId());
        groupUserService.addGroupUser(groupAndUserVO);
    }

    @Transactional
    @Override
    public void deleteGroup(GroupAndUserVO groupAndUserVO) {
        if (groupAndUserVO.getId() == null) {
            throw new IllegalArgumentException("参数不合法");
        }
        if (groupUserService.count(new LambdaQueryWrapper<GroupUser>().eq(GroupUser::getGroupId, groupAndUserVO.getId())) > 0) {
            throw new CustomException("当前组内仍存在成员,请先手动清除！");
        }
        this.removeById(groupAndUserVO.getId());
    }

    @Transactional
    @Override
    public void forceDeleteGroup(GroupAndUserVO groupAndUserVO) {
        if (groupAndUserVO.getId() == null) {
            throw new IllegalArgumentException("参数不合法");
        }
        this.removeById(groupAndUserVO.getId());
        groupUserService.remove(new LambdaQueryWrapper<GroupUser>().eq(GroupUser::getGroupId, groupAndUserVO.getId()));

    }

    @Transactional
    @Override
    public void updateGroup(GroupAndUserVO groupAndUserVO) {
        if (groupAndUserVO == null || groupAndUserVO.getGroupUserList() == null) {
            throw new IllegalArgumentException("参数不合法");
        }
        if (groupAndUserVO.getId() == null) {
            throw new IllegalArgumentException("参数不合法");
        }
        if (StringUtils.isEmpty(groupAndUserVO.getName())) {
            throw new IllegalArgumentException("参数不合法");
        }
        LambdaQueryWrapper<Group> groupLambdaQueryWrapper = new LambdaQueryWrapper<>();
        groupLambdaQueryWrapper.eq(Group::getId, groupAndUserVO.getId());
        Group one = this.getOne(groupLambdaQueryWrapper);
        if (one == null) {
            throw new RuntimeException("无法更新");
        }
        if ((!one.getName().equals(groupAndUserVO.getName())) && this.isDuplicate(groupAndUserVO.getName()).isDuplicate()) {
            throw new RuntimeException("重复组名！");
        }
        LambdaUpdateWrapper<Group> groupLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        groupLambdaUpdateWrapper.set(Group::getName, groupAndUserVO.getName());
        groupLambdaUpdateWrapper.eq(Group::getId, groupAndUserVO.getId());
        this.update(groupLambdaUpdateWrapper);
        // 更新组内成员信息 删除所有关联-->添加新关联
        LambdaQueryWrapper<GroupUser> groupUserLambdaQueryWrapper = new LambdaQueryWrapper<>();
        groupUserLambdaQueryWrapper.eq(GroupUser::getGroupId, groupAndUserVO.getId());
        groupUserService.remove(groupUserLambdaQueryWrapper);
        groupUserService.addGroupUser(groupAndUserVO);
        // 业务结束
    }

    /**
     * 用户关系表暂时前端分页
     *
     * @param pageNum  分页之当前多少页
     * @param pageSize 分页之每页多少条
     * @param name     模糊查询查询条件
     * @return
     */
    @Override
    public PageData<GroupAndUserFrontVO> queryGroup(Integer pageNum, Integer pageSize, String name) {

        if (pageNum == null) {
            pageNum = 1;
        }
        if (pageSize == null) {
            pageSize = 10;
        }
        if (pageSize > 100) {
            throw new IllegalArgumentException("每页条数不能大于100");
        }
        LambdaQueryWrapper<Group> groupLambdaQueryWrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isNotEmpty(name)) {
            groupLambdaQueryWrapper.like(Group::getName, name);
        }
        Page<Group> pageInfo = new Page<Group>(pageNum, pageSize);
        this.page(pageInfo, groupLambdaQueryWrapper);
        List<GroupAndUserFrontVO> results = new ArrayList<>();
        //处理数据
        pageInfo.getRecords().forEach(group -> {
            GroupAndUserFrontVO groupAndUserFrontVO = new GroupAndUserFrontVO();
            List<GroupUser> list = groupUserService.list(new LambdaQueryWrapper<GroupUser>().eq(GroupUser::getGroupId, group.getId()));
            List<GroupUserVO> groupUserVOList = new ArrayList<>();
            list.forEach(groupUser -> {
                GroupUserVO groupUserVO = new GroupUserVO();
                groupUserVO.setId(groupUser.getId());
                groupUserVO.setGroupId(groupUser.getGroupId());
                groupUserVO.setUserId(groupUser.getUserId());
                User byId = userService.getById(groupUser.getUserId());
                if (byId != null) {
                    groupUserVO.setUserName(byId.getName());
                    groupUserVO.setSex(byId.getSex());
                    groupUserVO.setStudentId(byId.getStudentId());
                } else {
                    groupUserVO.setUserName("");
                    groupUserVO.setSex("");
                    groupUserVO.setStudentId("");
                }
                groupUserVOList.add(groupUserVO);
            });
            groupAndUserFrontVO.setId(group.getId());
            groupAndUserFrontVO.setName(group.getName());
            groupAndUserFrontVO.setCount(list.size());
            // 获取创建人
            groupAndUserFrontVO.setCreateTime(group.getCreateTime());
            groupAndUserFrontVO.setCreateUserName(userService.getById(group.getCreateUser()).getName());
            groupAndUserFrontVO.setGroupUserVOList(groupUserVOList);
            results.add(groupAndUserFrontVO);
        });
        PageData<GroupAndUserFrontVO> pageData = new PageData<>();
        pageData.setPages(pageInfo.getPages());
        pageData.setTotal(pageInfo.getTotal());
        pageData.setCountId(pageInfo.getCountId());
        pageData.setCurrent(pageInfo.getCurrent());
        pageData.setSize(pageInfo.getSize());
        pageData.setRecords(results);
        pageData.setMaxLimit(pageInfo.getMaxLimit());
        return pageData;
    }

    @Override
    public Duplicate isDuplicate(String name) {
        LambdaQueryWrapper<Group> groupLambdaQueryWrapper = new LambdaQueryWrapper<>();
        groupLambdaQueryWrapper.eq(Group::getName, name);
        int count = this.count(groupLambdaQueryWrapper);
        if (count == 0) {
            return new Duplicate(false, null, null);
        }
        List<String> names = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            names.add(name + new Random().nextInt(1214 * (i + 1)));
        }
        return new Duplicate(true, "组名重复", names);
    }

    @Override
    public List<GroupTree> getCanBeAdd(String groupId) {
        //这些组id的成员放入未分组，其余在分组的用户放入分组，剩下的用户放入未分组
        Map<String, Object> groupMap = new HashMap<>();
        if (!StringUtils.isEmpty(groupId)) {
            if (groupId.contains(",")) {
                String[] split = groupId.split(",");
                for (String s :
                        split) {
                    groupMap.put(String.valueOf(s), null);
                }
            } else {
                groupMap.put(String.valueOf(groupId), null);
            }
        }
        //所有的组
        List<GroupTree> groupTrees = new ArrayList();
        //查询用户信息放入map,key为id,value为对象
        Map<String, User> userMap = new HashMap<>();
        List<User> userList = userService.list();
        userList.forEach(user -> {
            userMap.put(String.valueOf(user.getId()), user);
        });
        Map<String, User> userMap2 = new HashMap<>();
        userMap2.putAll(userMap);
        //获取所有组分的id
        LambdaQueryWrapper<Group> groupLambdaQueryWrapper = new LambdaQueryWrapper<>();
        groupLambdaQueryWrapper.select(Group::getId, Group::getName);
        List<Group> groupList = this.list(groupLambdaQueryWrapper);
        for (Group group :
                groupList) {
            if (groupMap.containsKey(String.valueOf(group.getId()))) {
                continue;
            }
            GroupTree groupTree1 = new GroupTree();
            groupTree1.setKey(group.getId());
            groupTree1.setTitle(group.getName());
            //该组的成员列表
            List<GroupTreeNode> groupTreeNodeList1 = new ArrayList<>();
            groupTree1.setChildren(groupTreeNodeList1);
            //查询该组组员id
            LambdaQueryWrapper<GroupUser> groupUserLambdaQueryWrapper = new LambdaQueryWrapper<>();
            groupUserLambdaQueryWrapper.eq(GroupUser::getGroupId, group.getId());
            groupUserLambdaQueryWrapper.select(GroupUser::getUserId);
            List<GroupUser> groupUserList = groupUserService.list(groupUserLambdaQueryWrapper);
            for (GroupUser groupUser :
                    groupUserList) {
                User user = userMap.get(String.valueOf(groupUser.getUserId()));
                GroupTreeNode groupTreeNode = new GroupTreeNode();
                groupTreeNode.setKey(groupUser.getUserId());
                User user1 = userMap.get(String.valueOf(groupUser.getUserId()));
                if (user1 == null) {
                    continue;
                }
                //不能直接删除来排除在组内的成员，map里的数据可能多个组要用，所以最好的方案就是再复制一下map，然后尝试删除即可，可能会有多次删除，后续都会抛出异常
                if (userMap2.containsKey(String.valueOf(groupUser.getUserId()))) {
                    userMap2.remove(String.valueOf(groupUser.getUserId()));
                }
                groupTreeNode.setTitle(user1.getName());
                groupTreeNodeList1.add(groupTreeNode);
            }
            groupTrees.add(groupTree1);
        }
        Collection<User> userNList = userMap2.values();

        //该组的成员列表
        List<GroupTreeNode> groupTreeNodeListN = new ArrayList<>();
        //未分组
        GroupTree groupTreeN = new GroupTree();
        groupTreeN.setKey(0L);
        groupTreeN.setTitle("未分组");
        groupTreeN.setChildren(groupTreeNodeListN);
        for (User user :
                userNList) {

            GroupTreeNode groupTreeNode = new GroupTreeNode();
            groupTreeNode.setKey(user.getId());
            groupTreeNode.setTitle(user.getName());
            groupTreeNodeListN.add(groupTreeNode);
        }
        groupTrees.add(groupTreeN);
        return groupTrees;
    }


    /**
     * 暂时前端有bug只允许获取未分组
     *
     * @param groupId
     * @return
     */
    @Override
    public List<GroupTree> getCanBeAddWFZ(String groupId) {
        //这些组id的成员放入未分组，其余在分组的用户放入分组，剩下的用户放入未分组
        Map<String, Object> groupMap = new HashMap<>();
        if (!StringUtils.isEmpty(groupId)) {
            if (groupId.contains(",")) {
                String[] split = groupId.split(",");
                for (String s :
                        split) {
                    groupMap.put(String.valueOf(s), null);
                }
            } else {
                groupMap.put(String.valueOf(groupId), null);
            }
        }
        //所有的组
        List<GroupTree> groupTrees = new ArrayList();
        //查询用户信息放入map,key为id,value为对象
        Map<String, User> userMap = new HashMap<>();
        List<User> userList = userService.list();
        userList.forEach(user -> {
            userMap.put(String.valueOf(user.getId()), user);
        });
        Map<String, User> userMap2 = new HashMap<>();
        userMap2.putAll(userMap);
        Collection<User> userNList = userMap2.values();
        //该组的成员列表
        List<GroupTreeNode> groupTreeNodeListN = new ArrayList<>();
        //未分组
        GroupTree groupTreeN = new GroupTree();
        groupTreeN.setKey(0L);
        groupTreeN.setTitle("未分组");
        groupTreeN.setChildren(groupTreeNodeListN);
        for (User user :
                userNList) {

            GroupTreeNode groupTreeNode = new GroupTreeNode();
            groupTreeNode.setKey(user.getId());
            groupTreeNode.setTitle(user.getName());
            groupTreeNodeListN.add(groupTreeNode);
        }
        groupTrees.add(groupTreeN);
        return groupTrees;
    }

}
