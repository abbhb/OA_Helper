package com.qc.printers.common.user.dao;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qc.printers.common.common.CustomException;
import com.qc.printers.common.common.domain.enums.NormalOrNoEnum;
import com.qc.printers.common.common.domain.vo.request.CursorPageBaseReq;
import com.qc.printers.common.common.domain.vo.response.CursorPageBaseResp;
import com.qc.printers.common.common.utils.CursorUtils;
import com.qc.printers.common.user.domain.entity.User;
import com.qc.printers.common.user.domain.enums.ChatActiveStatusEnum;
import com.qc.printers.common.user.mapper.UserMapper;
import com.qc.printers.common.user.service.cache.UserCache;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Transactional
@Service
public class UserDao extends ServiceImpl<UserMapper, User> {
    @Autowired
    private UserCache userCache;

    public CursorPageBaseResp<User> getCursorPage(List<Long> memberUidList, CursorPageBaseReq request, ChatActiveStatusEnum online) {
        return CursorUtils.getCursorPageByMysql(this, request, wrapper -> {
            wrapper.eq(User::getActiveStatus, online.getStatus());//筛选上线或者离线的
            wrapper.in(CollectionUtils.isNotEmpty(memberUidList), User::getId, memberUidList);//普通群对uid列表做限制
        }, User::getLoginDate);
    }

    public List<User> getMemberList() {
        return lambdaQuery()
                .eq(User::getStatus, NormalOrNoEnum.NORMAL.getStatus())
                .orderByDesc(User::getLoginDate)//最近活跃的1000个人，可以用lastOptTime字段，但是该字段没索引，updateTime可平替
                .last("limit 1000")//毕竟是大群聊，人数需要做个限制
                .select(User::getId, User::getName, User::getAvatar)
                .list();

    }

    public Integer getOnlineCount() {
        return getOnlineCount(null);
    }

    public Integer getOnlineCount(List<Long> memberUidList) {
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
        int count = this.count(lambdaQueryWrapper);
        if (count > 0) {
            throw new CustomException("用户名已经存在");
        }
        return this.save(entity);
    }


    @Transactional
    @Override
    public boolean saveOrUpdate(User entity) {
        return this.saveOrUpdate(entity);
    }

    @Transactional
    @Override
    public boolean saveOrUpdateBatch(Collection<User> entityList, int batchSize) {
        return this.saveOrUpdateBatch(entityList, batchSize);
    }


    @Override
    public boolean updateBatchById(Collection<User> entityList, int batchSize) {
        return this.updateBatchById(entityList, batchSize);
    }

    @Override
    public boolean saveOrUpdateBatch(Collection<User> entityList) {
        for (User user :
                entityList) {
            if (user.getId() != null) {
                userCache.userInfoChange(user.getId());
            }
        }
        return this.saveOrUpdateBatch(entityList);
    }

    @Override
    public boolean removeById(Serializable id) {
        userCache.delUserInfo((Long) id);
        return this.removeById(id);
    }

    @Transactional
    @Override
    public boolean removeByMap(Map<String, Object> columnMap) {
        throw new CustomException("防止没有更新缓存，禁用");
    }

    @Transactional
    @Override
    public boolean remove(Wrapper<User> queryWrapper) {
        User one = this.getOne(queryWrapper);
        userCache.delUserInfo(one.getId());
        return this.remove(queryWrapper);
    }

    @Override
    public boolean removeByIds(Collection<? extends Serializable> idList) {
        for (Serializable id :
                idList) {
            userCache.userInfoChange((Long) id);
        }
        return this.removeByIds(idList);
    }

    @Override
    public boolean updateById(User entity) {
        userCache.userInfoChange(entity.getId());
        return super.updateById(entity);
    }


    @Override
    public boolean update(Wrapper<User> updateWrapper) {
        User one = this.getOne(updateWrapper);
        userCache.userInfoChange(one.getId());
        return this.update(updateWrapper);
    }

    @Override
    public boolean update(User entity, Wrapper<User> updateWrapper) {
        User one = this.getOne(updateWrapper);
        userCache.userInfoChange(one.getId());
        return this.update(entity, updateWrapper);
    }

    @Override
    public boolean updateBatchById(Collection<User> entityList) {
        for (User user :
                entityList) {
            userCache.userInfoChange(user.getId());
        }
        return this.updateBatchById(entityList);
    }

    @Override
    public boolean saveOrUpdate(User entity, Wrapper<User> updateWrapper) {
        throw new CustomException("目的不明确");
    }

}