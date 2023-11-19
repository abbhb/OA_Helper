package com.qc.printers.custom.user.domain.dto;

import com.qc.printers.common.user.domain.entity.UserFrontConfig;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;
import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@ToString
@Data
public class UserFrontConfigRedisDto extends UserFrontConfig implements Serializable {
    // 只有更新时间长于1分钟才会更新数据库，否则不更新数据库，只变动redis
    private LocalDateTime lastUpdate;

}
