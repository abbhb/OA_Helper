package com.qc.printers.common.common.event.listener;

import com.qc.printers.common.common.event.UserOfflineEvent;
import com.qc.printers.common.common.utils.DateUtils;
import com.qc.printers.common.user.domain.entity.User;
import com.qc.printers.common.user.service.IUserService;
import com.qc.printers.common.user.service.WebSocketService;
import com.qc.printers.common.user.service.adapter.WSAdapter;
import com.qc.printers.common.user.service.cache.UserCache;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * 用户下线监听器
 *
 * @author zhongzb create on 2022/08/26
 */
@Slf4j
@Component
public class UserOfflineListener {
    @Autowired
    private WebSocketService webSocketService;
    @Autowired
    private IUserService iUserService;
    @Autowired
    private UserCache userCache;
    @Autowired
    private WSAdapter wsAdapter;

    @Async
    @EventListener(classes = UserOfflineEvent.class)
    public void saveRedisAndPush(UserOfflineEvent event) {
        User user = event.getUser();
        userCache.offline(user.getId(), DateUtils.localDateTimeToDate(user.getLoginDate()));
        //推送给所有在线用户，该用户下线
        webSocketService.sendToAllOnline(wsAdapter.buildOfflineNotifyResp(event.getUser()), event.getUser().getId());
    }

    @Async
    @EventListener(classes = UserOfflineEvent.class)
    public void saveDB(UserOfflineEvent event) {
        User user = event.getUser();
        User update = new User();
        update.setId(user.getId());
        update.setLoginDate(user.getLoginDate());
//        update.setActiveStatus(ChatActiveStatusEnum.OFFLINE.getStatus());
        iUserService.updateById(update);
    }

}
