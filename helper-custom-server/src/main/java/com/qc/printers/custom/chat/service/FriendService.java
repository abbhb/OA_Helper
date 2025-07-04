package com.qc.printers.custom.chat.service;


import com.qc.printers.common.chat.domain.vo.response.ChatMemberWListResp;
import com.qc.printers.common.common.domain.vo.request.CursorPageBaseReq;
import com.qc.printers.common.common.domain.vo.request.PageBaseReq;
import com.qc.printers.common.common.domain.vo.response.CursorPageBaseResp;
import com.qc.printers.common.common.domain.vo.response.PageBaseResp;
import com.qc.printers.common.user.domain.vo.request.friend.FriendApplyReq;
import com.qc.printers.common.user.domain.vo.request.friend.FriendApproveReq;
import com.qc.printers.common.user.domain.vo.request.friend.FriendCheckReq;
import com.qc.printers.common.user.domain.vo.response.friend.FriendApplyResp;
import com.qc.printers.common.user.domain.vo.response.friend.FriendCheckResp;
import com.qc.printers.common.user.domain.vo.response.friend.FriendResp;
import com.qc.printers.common.user.domain.vo.response.friend.FriendUnreadResp;

import java.util.List;

/**
 * @author : limeng
 * @description : 好友
 * @date : 2023/07/19
 */
public interface FriendService {

    /**
     * 检查
     * 检查是否是自己好友
     *
     * @param request 请求
     * @param uid     uid
     * @return {@link FriendCheckResp}
     */
    FriendCheckResp check(Long uid, FriendCheckReq request);

    /**
     * 应用
     * 申请好友
     *
     * @param request 请求
     * @param uid     uid
     */
    void apply(Long uid, FriendApplyReq request);

    /**
     * 分页查询好友申请
     *
     * @param request 请求
     * @return {@link PageBaseResp}<{@link FriendApplyResp}>
     */
    PageBaseResp<FriendApplyResp> pageApplyFriend(Long uid, PageBaseReq request);

    /**
     * 申请未读数
     *
     * @return {@link FriendUnreadResp}
     */
    FriendUnreadResp unread(Long uid);

    /**
     * 同意好友申请
     *
     * @param uid     uid
     * @param request 请求
     */
    void applyApprove(Long uid, FriendApproveReq request);

    /**
     * 删除好友
     *
     * @param uid       uid
     * @param friendUid 朋友uid
     */
    void deleteFriend(Long uid, Long friendUid);

    CursorPageBaseResp<FriendResp> friendList(Long uid, CursorPageBaseReq request);

    List<ChatMemberWListResp> getContactList(Long uid);
}
