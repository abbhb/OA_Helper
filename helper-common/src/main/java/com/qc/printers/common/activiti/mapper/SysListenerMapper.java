package com.qc.printers.common.activiti.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qc.printers.common.activiti.entity.SysListenerEntity;
import com.qc.printers.common.activiti.entity.dto.SysListenerListDto;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 执行监听器
 *
 * @author liuguofeng
 * @email liuguofeng-java@qq.com
 * @date 2023-12-06 22:18:27
 */
@Mapper
public interface SysListenerMapper extends BaseMapper<SysListenerEntity> {

    /**
     * 执行监听器分页列表
     *
     * @param dto 分页参数
     * @return 列表
     */
    List<SysListenerEntity> queryPage(SysListenerListDto dto);
}
