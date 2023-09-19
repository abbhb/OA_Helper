package com.qc.printers.common.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.qc.printers.common.common.domain.vo.request.CursorPageBaseReq;
import com.qc.printers.common.common.domain.vo.response.CursorPageBaseResp;
import com.qc.printers.common.user.domain.entity.User;
import com.qc.printers.common.user.domain.enums.ChatActiveStatusEnum;

import java.util.List;

public interface IUserService extends IService<User> {

    public CursorPageBaseResp<User> getCursorPage(List<Long> memberUidList, CursorPageBaseReq request, ChatActiveStatusEnum online);

    public List<User> getMemberList();

    public Integer getOnlineCount();

    public Integer getOnlineCount(List<Long> memberUidList);

}
