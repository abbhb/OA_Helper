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
    // 首先，获取 `must.type_n = 1` 的条件下相关的 msg_id


    String querySql = "SELECT message.*,must.state FROM message LEFT JOIN message_user_state must ON message.id = must.msg_id AND must.user_id = ${userid} WHERE ( must.state IS NULL OR must.state = 0 ) AND (( must.type_n != 1 ) OR (message.id > (SELECT MAX( must1.msg_id) AS last_msg_id FROM message_user_state must1 WHERE must1.user_id = ${userid} AND must1.room_id = message.room_id AND must1.type_n = 1 )) OR must.type_n IS NULL) ";

    String wrapperSql = "SELECT * from ( " + querySql + " ) AS q ${ew.customSqlSegment}";
    /**
     * 分页查询
     */
    @Select(wrapperSql)
    Page<MessageWithStateDto> page(Page page, @Param("ew") Wrapper queryWrapper, @Param("userid") Long userid);

}
