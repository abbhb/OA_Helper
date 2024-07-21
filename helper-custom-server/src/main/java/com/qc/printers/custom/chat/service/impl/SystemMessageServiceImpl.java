package com.qc.printers.custom.chat.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qc.printers.common.chat.dao.MessageDao;
import com.qc.printers.common.chat.dao.SystemMessageConfirmDao;
import com.qc.printers.common.chat.dao.SystemMessageDao;
import com.qc.printers.common.chat.domain.entity.Message;
import com.qc.printers.common.chat.domain.entity.SystemMessage;
import com.qc.printers.common.chat.domain.entity.SystemMessageConfirm;
import com.qc.printers.common.chat.domain.vo.response.ChatMessageResp;
import com.qc.printers.common.chat.domain.vo.response.SystemMessageResp;
import com.qc.printers.common.chat.service.adapter.MessageAdapter;
import com.qc.printers.common.common.utils.ThreadLocalUtil;
import com.qc.printers.common.common.utils.oss.OssDBUtil;
import com.qc.printers.common.user.dao.UserDao;
import com.qc.printers.common.user.domain.dto.UserInfo;
import com.qc.printers.common.user.domain.entity.User;
import com.qc.printers.custom.chat.service.SystemMessageService;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SystemMessageServiceImpl implements SystemMessageService {

    @Autowired
    private SystemMessageDao systemMessageDao;

    @Autowired
    private SystemMessageConfirmDao systemMessageConfirmDao;

    @Autowired
    private MessageDao messageDao;

    @Autowired
    private UserDao userDao;

    @Override
    public List<SystemMessageResp> list() {
        UserInfo currentUser = ThreadLocalUtil.getCurrentUser();
        List<SystemMessage> list = systemMessageDao.list();
        List<SystemMessageResp> result = new ArrayList<>();
        List<SystemMessageResp> yiduList = new ArrayList<>();

        Map<Long, SystemMessageConfirm> collect = systemMessageConfirmDao.list(new LambdaQueryWrapper<SystemMessageConfirm>()
                .eq(SystemMessageConfirm::getUserId, currentUser.getId()))
                .stream()
                .collect(Collectors.toMap(
                        SystemMessageConfirm::getSystemMessageId,
                        Function.identity()
                ));
        for (SystemMessage systemMessage : list) {
            if (!collect.containsKey(systemMessage.getId())){
                SystemMessageResp systemMessageResp = getSystemMessageResp(systemMessage,Boolean.FALSE);
                result.add(systemMessageResp);
                continue;
            }
            if (collect.get(systemMessage.getId()).getReadType().equals(2)){
                // 标记已读并删除了
                continue;
            }
            // 否则添加到另一个列表
            SystemMessageResp systemMessageResp = getSystemMessageResp(systemMessage,Boolean.TRUE);
            yiduList.add(systemMessageResp);
            continue;
        }
        result.sort(Comparator.comparing(SystemMessageResp::getCreateTime).reversed());
        yiduList.sort(Comparator.comparing(SystemMessageResp::getCreateTime).reversed());
        result.addAll(yiduList);
        return result;
    }

    @Override
    public Integer noreadCount() {
        List<SystemMessage> list = systemMessageDao.list();
        Integer tiaoshu = 0;
        UserInfo currentUser = ThreadLocalUtil.getCurrentUser();
        for (SystemMessage systemMessage : list) {
            LambdaQueryWrapper<SystemMessageConfirm> systemMessageConfirmLambdaQueryWrapper = new LambdaQueryWrapper<>();
            systemMessageConfirmLambdaQueryWrapper.eq(SystemMessageConfirm::getUserId,currentUser.getId());
            systemMessageConfirmLambdaQueryWrapper.eq(SystemMessageConfirm::getSystemMessageId,systemMessage.getId());
            systemMessageConfirmLambdaQueryWrapper.eq(SystemMessageConfirm::getReadType,1).or().eq(SystemMessageConfirm::getReadType,2);
            if (systemMessageConfirmDao.count(systemMessageConfirmLambdaQueryWrapper)<1){
                tiaoshu+=1;
            }
        }
        return tiaoshu;
    }

    @Override
    public String read(@NotNull Long id,@NotNull Integer type) {
        UserInfo currentUser = ThreadLocalUtil.getCurrentUser();
        SystemMessageConfirm one = systemMessageConfirmDao.getOne(new LambdaQueryWrapper<SystemMessageConfirm>()
                .eq(SystemMessageConfirm::getSystemMessageId, id)
                .eq(SystemMessageConfirm::getUserId,currentUser.getId())
        );
        if (one==null){
            one = new SystemMessageConfirm();
            one.setSystemMessageId(id);
            one.setReadType(type);
            one.setUserId(currentUser.getId());
            systemMessageConfirmDao.save(one);
            if (type.equals(2))return "删除成功";
            return "已读成功";
        }
        if (one.getReadType().equals(2)){
            // 此时已经删除
            return "已经删除";
        }
        one.setReadType(type);
        systemMessageConfirmDao.updateById(one);
        return "操作成功";
    }

    @NotNull
    private SystemMessageResp getSystemMessageResp(SystemMessage systemMessage,Boolean isRead) {
        SystemMessageResp systemMessageResp = new SystemMessageResp();
        Message message = messageDao.getById(systemMessage.getMsgId());
        ChatMessageResp chatMessageResp = MessageAdapter.buildChatMessageResp(message);
        systemMessageResp.setMessage(chatMessageResp);
        systemMessageResp.setRead(isRead);
        systemMessageResp.setCreateUser(systemMessage.getCreateUser());
        systemMessageResp.setCreateTime(systemMessage.getCreateTime());
        User user = userDao.getById(systemMessage.getCreateUser());
        systemMessageResp.setUsername(user.getName());
        systemMessageResp.setAvatar(OssDBUtil.toUseUrl(user.getAvatar()));
        systemMessageResp.setId(systemMessage.getId());
        return systemMessageResp;
    }
}
