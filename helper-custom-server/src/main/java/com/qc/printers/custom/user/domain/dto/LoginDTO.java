package com.qc.printers.custom.user.domain.dto;

import com.qc.printers.common.user.domain.entity.User;
import lombok.*;

/**
 * @EqualsAndHashCode
 * @ToString 用来解决子类log.info无法打印父类属性的问题
 */
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
@AllArgsConstructor//不加这个是没有有参构造的
@NoArgsConstructor
public class LoginDTO extends User {
    private Boolean week;
}
