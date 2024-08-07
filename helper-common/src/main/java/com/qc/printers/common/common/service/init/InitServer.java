package com.qc.printers.common.common.service.init;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qc.printers.common.chat.dao.RoomDao;
import com.qc.printers.common.chat.domain.entity.Room;
import com.qc.printers.common.chat.domain.enums.HotFlagEnum;
import com.qc.printers.common.chat.service.cache.HotRoomCache;
import com.qc.printers.common.common.CustomException;
import com.qc.printers.common.common.MyString;
import com.qc.printers.common.common.annotation.RedissonLock;
import com.qc.printers.common.common.service.CommonConfigService;
import com.qc.printers.common.common.utils.RSAUtil;
import com.qc.printers.common.common.utils.RedisUtils;
import com.qc.printers.common.config.system.signin.SigninTipMessageConfig;
import com.qc.printers.common.oauth.dao.SysOauthDao;
import com.qc.printers.common.oauth.domain.entity.SysOauth;
import com.qc.printers.common.oauth.service.OauthOpenidService;
import com.qc.printers.common.user.dao.UserDao;
import com.qc.printers.common.user.domain.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class InitServer {
    @Autowired
    private RoomDao roomDao;

    @Autowired
    private CommonConfigService commonConfigService;

    @Autowired
    private HotRoomCache hotRoomCache;

    @Autowired
    private OauthOpenidService oauthOpenidService;

    @Autowired
    private SysOauthDao sysOauthDao;

    @Autowired
    private UserDao userDao;

    @Autowired
    private SigninTipMessageConfig signinTipMessageConfig;

    private void commonConfigRedis() {
        //缓存公共配置
        commonConfigService.list();

    }

    private void RSAInit() {
        //创建rsa的key
        RSAUtil.createKey();
    }

    private void initChatRoomRedis() {
        LambdaQueryWrapper<Room> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Room::getHotFlag, HotFlagEnum.YES.getType());
        List<Room> list = roomDao.list(queryWrapper);
        for (Room room : list) {
            hotRoomCache.refreshActiveTime(room.getId(), room.getActiveTime());
        }
    }

    @RedissonLock(prefixKey = "Oauth_client_openid", key = "server-init", waitTime = 30, unit = TimeUnit.SECONDS, target = RedissonLock.Target.STR)
    private void initOauthOpenidMaxsRedis() {
        LocalDateTime now = LocalDateTime.now();

        if (RedisUtils.hasKey(MyString.oauth_client_init_time)) {
            LocalDateTime localDateTime = RedisUtils.get(MyString.oauth_client_init_time, LocalDateTime.class);
            Duration dur = Duration.between(localDateTime, now);
            if (dur.toMinutes() < 10l) {
                //10分钟内不重复更新
                return;
            }
        }
        LambdaQueryWrapper<SysOauth> sysOauthLambdaQueryWrapper = new LambdaQueryWrapper<>();
        sysOauthLambdaQueryWrapper.select(SysOauth::getId);
        for (SysOauth sysOauth : sysOauthDao.list(sysOauthLambdaQueryWrapper)) {
            oauthOpenidService.initRedisOpenidMax(sysOauth.getId());
        }
        RedisUtils.set(MyString.oauth_client_init_time, now);
    }

    private void initSigninWs(){
        // 先判断是否启用了
        if (!signinTipMessageConfig.isEnable())return;
        String userId = signinTipMessageConfig.getUserId();
        User user = userDao.getById(Long.valueOf(userId));
        if (user==null)throw new CustomException("打卡消息推送用户异常，请检查或关闭该服务!");
        // 创建一个该用户的永久登录的token
        RedisUtils.set("SIGNIN-TUISONG-LED-10012-01", userId);// 尝试设置，当存在也不会异常，存在try catch

    }
    public void init() {
        System.out.println("初始化服务");
        System.out.println("RSA初始化");
        RSAInit();
        System.out.println("公共配置初始化");
        commonConfigRedis();
        System.out.println("初始化chat-room缓存");
        initChatRoomRedis();
        System.out.println("初始化oauth-openid-max缓存");
        initOauthOpenidMaxsRedis();
        System.out.println("初始化大屏推送服务");
        initSigninWs();

    }
}
