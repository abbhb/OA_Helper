package com.qc.printers.common.user.dao;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qc.printers.common.common.domain.vo.request.CursorPageBaseReq;
import com.qc.printers.common.common.domain.vo.response.CursorPageBaseResp;
import com.qc.printers.common.common.utils.CursorUtils;
import com.qc.printers.common.user.domain.entity.UserFriend;
import com.qc.printers.common.user.mapper.UserFriendMapper;
import org.springframework.stereotype.Service;

import java.util.List;


/**
 * <p>
 * 用户联系人表 服务实现类
 * </p>
 *
 * @author <a href="https://github.com/zongzibinbin">abin</a>
 * @since 2023-07-16
 */
@Service
public class UserFriendDao extends ServiceImpl<UserFriendMapper, UserFriend> {

    public List<UserFriend> getByFriends(Long uid, List<Long> uidList) {
        return lambdaQuery().eq(UserFriend::getUid, uid)
                .in(UserFriend::getFriendUid, uidList)
                .list();
    }

    public UserFriend getByFriend(Long uid, Long targetUid) {
        return lambdaQuery().eq(UserFriend::getUid, uid)
                .eq(UserFriend::getFriendUid, targetUid)
                .one();
    }



    public List<UserFriend> getUserFriend(Long uid, Long friendUid) {
        return lambdaQuery()
                .eq(UserFriend::getUid, uid)
                .eq(UserFriend::getFriendUid, friendUid)
                .or()
                .eq(UserFriend::getFriendUid, uid)
                .eq(UserFriend::getUid, friendUid)
                .select(UserFriend::getId)
                .list();
    }

}
