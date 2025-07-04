package com.qc.printers.common.chat.service;

import com.qc.printers.common.chat.domain.entity.RoomFriend;
import com.qc.printers.common.chat.domain.entity.RoomGroup;
import com.qc.printers.common.chat.domain.vo.request.ChatRemarkSetReq;

import java.util.List;

/**
 * Description: 房间底层管理
 * Author: <a href="https://github.com/zongzibinbin">abin</a>
 * Date: 2023-07-22
 */
public interface RoomService {

    /**
     * 创建一个单聊房间
     */
    RoomFriend createFriendRoom(Long uid,Long friendUid);

    RoomFriend getFriendRoom(Long uid1, Long uid2);

    /**
     * 禁用一个单聊房间
     */
    void disableFriendRoom(List<Long> uidList);


    /**
     * 创建一个群聊房间
     */
    RoomGroup createGroupRoom(Long uid);


    String setRemark(Long uid, ChatRemarkSetReq request);
}
