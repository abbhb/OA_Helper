package com.qc.printers.common.signin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qc.printers.common.signin.domain.entity.SigninRenewal;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;

@Mapper
public interface SigninRenewalMapper extends BaseMapper<SigninRenewal> {

    @Select("SELECT * FROM signin_renewal WHERE user_id = #{userId} AND renewal_time BETWEEN #{startTime} AND #{endTime} ORDER BY CASE WHEN state = 1 THEN 1 WHEN state = 0 THEN 2 WHEN state = 2 THEN 3 END, update_time DESC LIMIT 1;")
    SigninRenewal hasExistRenewal(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime, @Param("userId") Long userId);

}
