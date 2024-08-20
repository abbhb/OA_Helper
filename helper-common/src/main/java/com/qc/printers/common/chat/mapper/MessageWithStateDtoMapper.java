package com.qc.printers.common.chat.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qc.printers.common.chat.domain.entity.MessageWithStateDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface MessageWithStateDtoMapper extends BaseMapper<MessageWithStateDto> {
    String querySql = "select message.*,must.state from message LEFT JOIN message_user_state must ON message.id = must.msg_id AND must.user_id = ${userid} where must.state IS NULL OR must.state = 0 ";
    String wrapperSql = "SELECT * from ( " + querySql + " ) AS q ${ew.customSqlSegment}";
    /**
     * 分页查询
     */
    @Select(wrapperSql)
    Page<MessageWithStateDto> page(Page page, @Param("ew") Wrapper queryWrapper, @Param("userid") Long userid);

}
