package com.qc.printers.common.user.domain.vo.response.user;

import com.qc.printers.common.user.domain.dto.UserInfoBaseExtDto;
import lombok.Data;

import java.io.Serializable;

@Data
public class UserInfoBaseExtStateResp implements Serializable {
    private Boolean state;

    private UserInfoBaseExtDto currentInfo;

    private UserInfoBaseExtDto newInfo;
}
