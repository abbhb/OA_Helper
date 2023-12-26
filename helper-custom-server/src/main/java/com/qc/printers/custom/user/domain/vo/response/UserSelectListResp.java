package com.qc.printers.custom.user.domain.vo.response;

import com.qc.printers.common.user.domain.entity.User;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class UserSelectListResp implements Serializable {
    private List<User> options;// 只包含id和name
}
