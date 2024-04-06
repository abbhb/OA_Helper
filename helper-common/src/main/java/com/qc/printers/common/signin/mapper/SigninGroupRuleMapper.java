package com.qc.printers.common.signin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qc.printers.common.signin.domain.entity.SigninGroupRule;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SigninGroupRuleMapper extends BaseMapper<SigninGroupRule> {
    @Select("SELECT COUNT(*) FROM signin_group_rule WHERE JSON_CONTAINS(rules_info->'$.kqsj[*].bcId', #{value})")
    Integer countSigninGroupByJsonKey(@Param("value") String value);

    @Select("SELECT * FROM signin_group_rule WHERE JSON_CONTAINS(rules_info->'$.kqsj[*].bcId', #{value})")
    List<SigninGroupRule> listSigninGroupByJsonKey(@Param("value") String value);
}
