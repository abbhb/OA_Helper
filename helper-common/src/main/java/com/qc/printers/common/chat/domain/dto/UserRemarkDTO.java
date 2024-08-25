package com.qc.printers.common.chat.domain.dto;

import com.qc.printers.common.user.domain.entity.User;
import lombok.*;

import java.io.Serializable;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class UserRemarkDTO extends User implements Serializable{

    /**
     * 关键就是为了备注名
     */
    private String remarkName;
}
