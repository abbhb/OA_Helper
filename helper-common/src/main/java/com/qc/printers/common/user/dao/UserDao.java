package com.qc.printers.common.user.dao;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qc.printers.common.common.CustomException;
import com.qc.printers.common.common.annotation.DataScope;
import com.qc.printers.common.common.domain.enums.NormalOrNoEnum;
import com.qc.printers.common.common.domain.vo.request.CursorPageBaseReq;
import com.qc.printers.common.common.domain.vo.response.CursorPageBaseResp;
import com.qc.printers.common.common.utils.CursorUtils;
import com.qc.printers.common.common.utils.oss.OssDBUtil;
import com.qc.printers.common.user.domain.entity.SysDept;
import com.qc.printers.common.user.domain.entity.User;
import com.qc.printers.common.user.domain.enums.ChatActiveStatusEnum;
import com.qc.printers.common.user.mapper.UserMapper;
import com.qc.printers.common.user.service.cache.UserCache;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Transactional
@Service
public class UserDao extends ServiceImpl<UserMapper, User> {


    @DataScope(userAlias = "user")
    public List<User> listUserWithScope(User user){
        LambdaQueryWrapper<User> userLambdaQueryWrapper = new LambdaQueryWrapper<>();
        userLambdaQueryWrapper.apply(com.qc.printers.common.common.utils.StringUtils.isNotEmpty(user.getExistSql()),user.getExistSql());
        return this.list(userLambdaQueryWrapper);
    }

   @DataScope(userAlias = "user")
    public List<Long> listUserIdsWithScope(User user){
        LambdaQueryWrapper<User> userLambdaQueryWrapper = new LambdaQueryWrapper<>();
        userLambdaQueryWrapper.select(User::getId);
        userLambdaQueryWrapper.apply(com.qc.printers.common.common.utils.StringUtils.isNotEmpty(user.getExistSql()),user.getExistSql());
        return this.list(userLambdaQueryWrapper).stream().map(User::getId).toList();
    }


    public CursorPageBaseResp<User> getCursorPage(List<Long> memberUidList, CursorPageBaseReq request, ChatActiveStatusEnum online,String search) {
        return CursorUtils.getCursorPageByMysql(this, request, wrapper -> {
            wrapper.eq(User::getActiveStatus, online.getStatus());//筛选上线或者离线的
            wrapper.and(StringUtils.isNotEmpty(search),userLambdaQueryWrapper -> userLambdaQueryWrapper.like(User::getStudentId,search).or().like(User::getName,search).or().like(User::getUsername,search));
            wrapper.in(CollectionUtils.isNotEmpty(memberUidList), User::getId, memberUidList);//普通群对uid列表做限制
            wrapper.orderByDesc(User::getLoginDate);
        }, User::getLoginDate);
    }

    public List<User> getMemberList() {
        return lambdaQuery()
                .eq(User::getStatus, NormalOrNoEnum.NORMAL.getStatus())
                .orderByDesc(User::getLoginDate)//最近活跃的1000个人，可以用lastOptTime字段，但是该字段没索引，updateTime可平替
                .last("limit 1000")//毕竟是大群聊，人数需要做个限制
                .select(User::getId, User::getName, User::getAvatar)
                .list().stream().map(user -> {
                    User user1 = new User();
                    user1.setId(user.getId());
                    user1.setName(user.getName());
                    user1.setAvatar(OssDBUtil.toUseUrl(user.getAvatar()));
                    return user1;
                }).collect(Collectors.toList());

    }

    public Long getOnlineCount() {
        return getOnlineCount(null);
    }

    public Long getOnlineCount(List<Long> memberUidList) {
        return lambdaQuery()
                .eq(User::getActiveStatus, ChatActiveStatusEnum.ONLINE.getStatus())
                .in(CollectionUtil.isNotEmpty(memberUidList), User::getId, memberUidList)
                .count();
    }

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
        long count = this.count(lambdaQueryWrapper);
        if (count > 0L) {
            throw new CustomException("用户名已经存在");
        }
        return super.save(entity);
    }

    public List<User> getFriendList(List<Long> uids) {
        return lambdaQuery()
                .in(User::getId, uids)
                .select(User::getId, User::getActiveStatus, User::getName, User::getAvatar)
                .list();

    }


    @Transactional
    @Override
    public boolean saveOrUpdate(User entity) {
        return super.saveOrUpdate(entity);
    }

    @Transactional
    @Override
    public boolean saveOrUpdateBatch(Collection<User> entityList, int batchSize) {
        return super.saveOrUpdateBatch(entityList, batchSize);
    }




    @Transactional
    @Override
    public boolean removeByMap(Map<String, Object> columnMap) {
        throw new CustomException("防止没有更新缓存，禁用");
    }



    @Override
    public boolean saveOrUpdate(User entity, Wrapper<User> updateWrapper) {
        throw new CustomException("目的不明确");
    }

    public List<User> findByRsaPasswordIsNotNull() {
        LambdaQueryWrapper<User> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.isNotNull(User::getRsaPassword);
        List<User> list = this.list(lambdaQueryWrapper);
        List<User> userLists = new ArrayList<>();
        for (User user : list) {
            if (StringUtils.isNotEmpty(user.getRsaPassword()) && !user.getRsaPassword().isEmpty()) {
                userLists.add(user);
            }
        }
        return userLists;
    }
}
