package com.qc.printers.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qc.printers.common.CustomException;
import com.qc.printers.common.MyString;
import com.qc.printers.mapper.CommonConfigMapper;
import com.qc.printers.pojo.CommonConfig;
import com.qc.printers.service.CommonConfigService;
import com.qc.printers.service.IRedisService;
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

    private final IRedisService iRedisService;


    public CommonConfigServiceImpl(IRedisService iRedisService) {
        this.iRedisService = iRedisService;
    }

    @Transactional
    @Override
    public boolean save(CommonConfig entity) {
        boolean a = super.save(entity);
        //更新redis
        List<CommonConfig> commonConfigs = (List<CommonConfig>) iRedisService.get(MyString.pre_common_config);
        if (commonConfigs == null) {
            commonConfigs = new ArrayList<>();
            commonConfigs.add(entity);
        } else {
            commonConfigs.add(entity);
        }
        iRedisService.set(MyString.pre_common_config, commonConfigs);
        return a;
    }

    @Transactional
    @Override
    public boolean removeById(Serializable id) {
        boolean a = super.removeById(id);
        List<CommonConfig> commonConfigs = (List<CommonConfig>) iRedisService.get(MyString.pre_common_config);
        if (commonConfigs != null) {
            Iterator iterator = commonConfigs.iterator();
            while (iterator.hasNext()) {
                CommonConfig commonConfig = (CommonConfig) iterator.next();
                if (commonConfig.getKey().equals(id)) {
                    iterator.remove();
                }
            }
            iRedisService.set(MyString.pre_common_config, commonConfigs);

        }
        return a;
    }

    @Transactional
    @Override
    public boolean remove(Wrapper<CommonConfig> queryWrapper) {
        throw new CustomException("禁止通过此方法删除");
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
        iRedisService.set(MyString.pre_common_config, list);
        return a;
    }

    @Override
    public boolean update(Wrapper<CommonConfig> updateWrapper) {
        boolean a = super.update(updateWrapper);
        List<CommonConfig> list = super.list();
        iRedisService.set(MyString.pre_common_config, list);
        return a;
    }

    @Transactional
    @Override
    public boolean update(CommonConfig entity, Wrapper<CommonConfig> updateWrapper) {
        boolean a = super.update(entity, updateWrapper);
        List<CommonConfig> list = super.list();
        iRedisService.set(MyString.pre_common_config, list);
        return a;
    }

    @Transactional
    @Override
    public boolean updateBatchById(Collection<CommonConfig> entityList) {
        boolean a = super.updateBatchById(entityList);
        List<CommonConfig> list = super.list();
        iRedisService.set(MyString.pre_common_config, list);
        return a;
    }

    @Override
    public CommonConfig getById(Serializable id) {
        List<CommonConfig> commonConfigs = (List<CommonConfig>) iRedisService.get(MyString.pre_common_config);
        if (commonConfigs == null) {
            List<CommonConfig> list = super.list();
            iRedisService.set(MyString.pre_common_config, list);
            commonConfigs = list;
        }
        List<CommonConfig> collect = commonConfigs.stream().filter(item -> item.getKey().equals(id)).collect(Collectors.toList());
        if (collect == null) {
            collect = new ArrayList<>();
        }
        if (collect.size() == 0) {
            CommonConfig commonConfig = super.getById(id);
            collect.add(commonConfig);
        }
        return collect.get(0);
    }

    @Override
    public CommonConfig getOne(Wrapper<CommonConfig> queryWrapper) {
        throw new CustomException("禁止此方法获取");
    }

    @Override
    public List<CommonConfig> list(Wrapper<CommonConfig> queryWrapper) {
        throw new CustomException("禁止此方法获取");
    }

    @Override
    public List<CommonConfig> list() {
        List<CommonConfig> commonConfigs = (List<CommonConfig>) iRedisService.get(MyString.pre_common_config);
        if (commonConfigs == null) {
            List<CommonConfig> list = super.list();
            iRedisService.set(MyString.pre_common_config, list);
            commonConfigs = list;
        }
        return commonConfigs;
    }
}
