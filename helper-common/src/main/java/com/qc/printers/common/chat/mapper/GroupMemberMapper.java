package com.qc.printers.common.chat.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qc.printers.common.chat.domain.entity.GroupMember;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * <p>
 * 群成员表 Mapper 接口
 * </p>
 *
 * @author <a href="https://github.com/zongzibinbin">abin</a>
 * @since 2023-07-16
 */
@Mapper
public interface GroupMemberMapper extends BaseMapper<GroupMember> {

    // 使用@Select注解来执行SQL查询
    @Select("SELECT DISTINCT `group_id` FROM `group_member` WHERE `uid` = #{uid}")
    List<Long> selectGroupIdsByUid(Long uid);

    /**
     * 此room下的所有成员id
     * @param groupId
     * @return
     */
    @Select("SELECT `uid` FROM (SELECT DISTINCT `uid`, `role`  FROM `group_member` WHERE `group_id` = #{groupId} ORDER BY `role` ASC) AS subquery")
    List<Long> selectUidsByGroupId(Long groupId);

}
