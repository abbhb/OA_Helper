package com.qc.printers.custom.signin.domain.vo;

import com.qc.printers.common.user.domain.entity.User;
import com.qc.printers.custom.user.domain.vo.response.UserResult;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class SigninDataResp extends UserResult {
    private boolean existFace;

    private boolean existCard;

    private String cardId;


}
