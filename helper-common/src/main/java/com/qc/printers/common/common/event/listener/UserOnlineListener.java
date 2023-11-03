package com.qc.printers.common.common.event.listener;

import com.qc.printers.common.common.event.UserOnlineEvent;
import com.qc.printers.common.common.utils.DateUtils;
import com.qc.printers.common.user.dao.UserDao;
import com.qc.printers.common.user.domain.entity.User;
import com.qc.printers.common.user.domain.enums.ChatActiveStatusEnum;
import com.qc.printers.common.user.service.IpService;
import com.qc.printers.common.user.service.WebSocketService;
import com.qc.printers.common.user.service.adapter.WSAdapter;
import com.qc.printers.common.user.service.cache.UserCache;
import com.qc.printers.common.user.service.impl.PushService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * 用户上线监听器
 *
 * @author zhongzb create on 2022/08/26
 */
@Slf4j
@Component
public class UserOnlineListener {
    @Autowired
    private WebSocketService webSocketService;
    @Autowired
    private UserDao userDao;
    @Autowired
    private UserCache userCache;
    @Autowired
    private WSAdapter wsAdapter;
    @Autowired
    private IpService ipService;
    @Autowired
    private PushService pushService;

    @Async
    @EventListener(classes = UserOnlineEvent.class)
    public void saveRedisAndPush(UserOnlineEvent event) {
        User user = event.getUser();
        userCache.online(user.getId(), DateUtils.localDateTimeToDate(user.getLoginDate()));
        //推送给所有在线用户，该用户登录成功
        pushService.sendPushMsg(wsAdapter.buildOnlineNotifyResp(event.getUser()));
    }

    @Async
    @EventListener(classes = UserOnlineEvent.class)
    public void saveDB(UserOnlineEvent event) {
        User user = event.getUser();
        User update = new User();
        update.setId(user.getId());
        update.setLoginDate(user.getLoginDate());
        update.setLoginIp(user.getLoginIp());
        update.setActiveStatus(ChatActiveStatusEnum.ONLINE.getStatus());
        userDao.updateById(update);
        //更新用户ip详情
        ipService.refreshIpDetailAsync(user.getId());
    }

}
