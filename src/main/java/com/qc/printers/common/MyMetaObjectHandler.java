package com.qc.printers.common;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.qc.printers.pojo.User;
import com.qc.printers.utils.ThreadLocalUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

//元数据处理器
@Slf4j
@Component
public class MyMetaObjectHandler implements MetaObjectHandler {

    @Autowired
    private HttpServletRequest httpServletRequest;
    @Override
    public void insertFill(MetaObject metaObject) {
        //此处可能会出现异常,要是userid为null此处异常
        if (metaObject.hasSetter("updateTime")) {
            metaObject.setValue("updateTime", LocalDateTime.now());
        }
        if (metaObject.hasSetter("createTime")) {
            metaObject.setValue("createTime", LocalDateTime.now());
        }

        if (metaObject.hasSetter("createUser")) {
            // 当需要获取创建人必定该接口是要校验是否登录
            // 所以currentUser在此处应该要存才，否则抛出异常
            User currentUser = ThreadLocalUtil.getCurrentUser();
            if (currentUser == null) {
                throw new RuntimeException("取参异常:My-1");
            }
            metaObject.setValue("createUser", currentUser.getId());
        }
        if (metaObject.hasSetter("isDeleted")) {
            metaObject.setValue("isDeleted", Integer.valueOf(0));//刚插入默认都是不删除的
        }

        if (metaObject.hasSetter("version")) {
            metaObject.setValue("version", Integer.valueOf(0));
        }

    }

    @Override
    public void updateFill(MetaObject metaObject) {
        log.info("公共字段自动填充");
        metaObject.setValue("updateTime",LocalDateTime.now());
    }
}
