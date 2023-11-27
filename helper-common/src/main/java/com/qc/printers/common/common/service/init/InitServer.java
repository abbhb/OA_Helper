package com.qc.printers.common.common.service.init;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qc.printers.common.chat.dao.RoomDao;
import com.qc.printers.common.chat.domain.entity.Room;
import com.qc.printers.common.chat.domain.enums.HotFlagEnum;
import com.qc.printers.common.chat.service.cache.HotRoomCache;
import com.qc.printers.common.common.service.CommonConfigService;
import com.qc.printers.common.common.utils.RSAUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InitServer {
    @Autowired
    private RoomDao roomDao;

    @Autowired
    private CommonConfigService commonConfigService;

    @Autowired
    private HotRoomCache hotRoomCache;

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

    public void init() {
        System.out.println("初始化服务");
        System.out.println("RSA初始化");
        RSAInit();
        System.out.println("公共配置初始化");
        commonConfigRedis();
        System.out.println("初始化chat-room缓存");
        initChatRoomRedis();

    }
}
