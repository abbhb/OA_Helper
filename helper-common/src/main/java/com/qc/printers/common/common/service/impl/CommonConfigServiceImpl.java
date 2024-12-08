package com.qc.printers.common.common.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qc.printers.common.common.MyString;
import com.qc.printers.common.common.domain.entity.CommonConfig;
import com.qc.printers.common.common.mapper.CommonConfigMapper;
import com.qc.printers.common.common.service.CommonConfigService;
import com.qc.printers.common.common.utils.RedisUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;


/**
 * 配置表
 * 暂时的缓存方式就是更新操作时同步redis
 */
@Slf4j
@Service
public class CommonConfigServiceImpl extends ServiceImpl<CommonConfigMapper, CommonConfig> implements CommonConfigService {


    @Transactional
    @Override
    public boolean save(CommonConfig entity) {
        boolean a = super.save(entity);
        //更新redis

        List<CommonConfig> commonConfigs = (List<CommonConfig>) RedisUtils.get(MyString.pre_common_config, List.class);
        if (commonConfigs == null) {
            commonConfigs = new ArrayList<>();
            commonConfigs.add(entity);
        } else {
            commonConfigs.add(entity);
        }
        RedisUtils.set(MyString.pre_common_config, commonConfigs);
        return a;
    }

    @Transactional
    @Override
    public boolean removeById(Serializable id) {
        boolean a = super.removeById(id);
        List<CommonConfig> commonConfigs = (List<CommonConfig>) RedisUtils.get(MyString.pre_common_config, List.class);
        if (commonConfigs != null) {
            Iterator iterator = commonConfigs.iterator();
            while (iterator.hasNext()) {
                CommonConfig commonConfig = (CommonConfig) iterator.next();
                if (commonConfig.getConfigKey().equals(id)) {
                    iterator.remove();
                }
            }
            RedisUtils.set(MyString.pre_common_config, commonConfigs);
        }
        return a;
    }


    @Transactional
    @Override
    public boolean removeByIds(Collection<? extends Serializable> idList) {
        for (Serializable id : idList
        ) {
            this.removeById(id);
        }
        return true;
    }

    @Override
    public boolean updateById(CommonConfig entity) {
        boolean a = super.updateById(entity);
        List<CommonConfig> list = super.list();
        RedisUtils.set(MyString.pre_common_config, list);
        return a;
    }

    @Transactional
    @Override
    public boolean update(Wrapper<CommonConfig> updateWrapper) {
        boolean a = super.update(updateWrapper);
        List<CommonConfig> list = super.list();
        RedisUtils.set(MyString.pre_common_config, list);
        return a;
    }

    @Transactional
    @Override
    public boolean update(CommonConfig entity, Wrapper<CommonConfig> updateWrapper) {
        boolean a = super.update(entity, updateWrapper);
        List<CommonConfig> list = super.list();
        RedisUtils.set(MyString.pre_common_config, list);
        return a;
    }

    @Transactional
    @Override
    public boolean updateBatchById(Collection<CommonConfig> entityList) {
        boolean a = super.updateBatchById(entityList);
        List<CommonConfig> list = super.list();
        RedisUtils.set(MyString.pre_common_config, list);
        return a;
    }
    @Transactional
    @Override
    public CommonConfig getById(Serializable id) {
        List<CommonConfig> commonConfigs = (List<CommonConfig>) RedisUtils.get(MyString.pre_common_config, List.class);
        if (commonConfigs == null) {
            List<CommonConfig> list = super.list();
            RedisUtils.set(MyString.pre_common_config, list);
            commonConfigs = list;
        }
        ObjectMapper objectMapper = new ObjectMapper();
        // 不加会报错
        commonConfigs = objectMapper.convertValue(commonConfigs, new TypeReference<List<CommonConfig>>() {});
        List<CommonConfig> collect = commonConfigs.stream().filter(item -> item.getConfigKey().equals(id)).collect(Collectors.toList());
        if (collect == null) {
            collect = new ArrayList<>();
        }
        if (collect.size() == 0) {
            CommonConfig commonConfig = super.getById(id);
            collect.add(commonConfig);
        }
        return collect.get(0);
    }


    @Transactional
    @Override
    public List<CommonConfig> list() {
        List<CommonConfig> commonConfigs = (List<CommonConfig>) RedisUtils.get(MyString.pre_common_config, List.class);
        if (commonConfigs == null) {
            List<CommonConfig> list = super.list();
            RedisUtils.set(MyString.pre_common_config, list);
            commonConfigs = list;
        }
        return commonConfigs;
    }



}
