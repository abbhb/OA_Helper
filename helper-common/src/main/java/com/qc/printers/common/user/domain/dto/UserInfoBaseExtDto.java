package com.qc.printers.common.user.domain.dto;

import com.qc.printers.common.user.domain.entity.UserExtBase;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.validation.constraints.NotEmpty;
import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class UserInfoBaseExtDto extends UserExtBase implements Serializable {

    @NotEmpty(message = "性别不能为空")
    private String sex;        // 性别

    @NotEmpty(message = "学号不能为空")
    private String studentId;  // 学号

    @NotEmpty(message = "手机号不能为空")
    private String phone;      // 手机号


}
