package com.qc.printers.common.chat.service.strategy.msg;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import com.qc.printers.common.chat.dao.MessageDao;
import com.qc.printers.common.chat.domain.entity.Message;
import com.qc.printers.common.chat.domain.entity.msg.MessageExtra;
import com.qc.printers.common.chat.domain.enums.MessageStatusEnum;
import com.qc.printers.common.chat.domain.enums.MessageTypeEnum;
import com.qc.printers.common.chat.domain.vo.request.ChatMessageReq;
import com.qc.printers.common.chat.domain.vo.request.msg.TextMsgReq;
import com.qc.printers.common.chat.domain.vo.response.msg.TextMsgResp;
import com.qc.printers.common.chat.service.adapter.MessageAdapter;
import com.qc.printers.common.chat.service.cache.MsgCache;
import com.qc.printers.common.common.domain.enums.YesOrNoEnum;
import com.qc.printers.common.common.utils.AssertUtil;
import com.qc.printers.common.common.utils.discover.PrioritizedUrlDiscover;
import com.qc.printers.common.common.utils.discover.domain.UrlInfo;
import com.qc.printers.common.common.utils.sensitiveWord.SensitiveWordBs;
import com.qc.printers.common.user.domain.dto.UserInfo;
import com.qc.printers.common.user.domain.entity.User;
import com.qc.printers.common.user.service.cache.UserCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Description: 普通文本消息
 * Author: <a href="https://github.com/zongzibinbin">abin</a>
 * Date: 2023-06-04
 */
@Component
public class TextMsgHandler extends AbstractMsgHandler {
    private static final PrioritizedUrlDiscover URL_TITLE_DISCOVER = new PrioritizedUrlDiscover();
    @Autowired
    private MessageDao messageDao;
    @Autowired
    private MsgCache msgCache;
    @Autowired
    private UserCache userCache;
    @Autowired
    private SensitiveWordBs sensitiveWordBs;

    @Override
    MessageTypeEnum getMsgTypeEnum() {
        return MessageTypeEnum.TEXT;
    }

    @Override
    public void checkMsg(ChatMessageReq request, Long uid) {
        TextMsgReq body = BeanUtil.toBean(request.getBody(), TextMsgReq.class);
        AssertUtil.allCheckValidateThrow(body);
        //校验下回复消息
        if (Objects.nonNull(body.getReplyMsgId())) {
            Message replyMsg = messageDao.getById(body.getReplyMsgId());
            AssertUtil.isNotEmpty(replyMsg, "回复消息不存在");
            AssertUtil.equal(replyMsg.getRoomId(), request.getRoomId(), "只能回复相同会话内的消息");
        }
        if (CollectionUtil.isNotEmpty(body.getAtUidList())) {
            List<Long> atUidList = body.getAtUidList();
            Set<Long> collect = new HashSet<>(atUidList);
            Map<Long, UserInfo> batch = userCache.getUserInfoBatch(collect);
            AssertUtil.equal(atUidList.size(), batch.values().size(), "@用户不存在");
            boolean atAll = body.getAtUidList().contains(0L);
            if (atAll) {
                // @全体成员权限判断
                // 后期升级群聊包含管理员,管理员权重1000，群主权重10000，按权重判断权限
//                AssertUtil.isTrue(iRoleService.hasPower(uid, RoleEnum.CHAT_MANAGER), "没有权限");

            }
        }
    }

    @Override
    public void saveMsg(Message msg, ChatMessageReq request) {//插入文本内容
        TextMsgReq body = BeanUtil.toBean(request.getBody(), TextMsgReq.class);
        MessageExtra extra = Optional.ofNullable(msg.getExtra()).orElse(new MessageExtra());
        Message update = new Message();
        update.setId(msg.getId());
        update.setContent(sensitiveWordBs.filter(body.getContent()));
        update.setExtra(extra);
        //如果有回复消息
        if (Objects.nonNull(body.getReplyMsgId())) {
            Integer gapCount = messageDao.getGapCount(request.getRoomId(), body.getReplyMsgId(), msg.getId());
            update.setGapCount(gapCount);
            update.setReplyMsgId(body.getReplyMsgId());

        }
        //判断消息url跳转
        extra.setUrlContentMap(null);
        if (body.getContent().length() <= 500) {
            Map<String, UrlInfo> urlContentMap = URL_TITLE_DISCOVER.getUrlContentMap(body.getContent());
            extra.setUrlContentMap(urlContentMap);
        }
        //艾特功能
        if (CollectionUtil.isNotEmpty(body.getAtUidList())) {
            extra.setAtUidList(body.getAtUidList().stream().map(String::valueOf).collect(Collectors.toSet()));
        }
        messageDao.updateById(update);
    }

    @Override
    public Object showMsg(Message msg) {
        TextMsgResp resp = new TextMsgResp();
        resp.setContent(msg.getContent());
        resp.setUrlContentMap(Optional.ofNullable(msg.getExtra()).map(MessageExtra::getUrlContentMap).orElse(null));
        if (msg.getExtra().getAtUidList() != null && msg.getExtra().getAtUidList().size() > 0) {
            List<String> collect = msg.getExtra().getAtUidList().stream().map(String::valueOf).collect(Collectors.toList());
            resp.setAtUidList(collect);
        } else {
            resp.setAtUidList(null);
        }

        //回复消息
        Optional<Message> reply = Optional.ofNullable(msg.getReplyMsgId())
                .map(msgCache::getMsg)
                .filter(a -> Objects.equals(a.getStatus(), MessageStatusEnum.NORMAL.getStatus()));
        if (reply.isPresent()) {
            Message replyMessage = reply.get();
            TextMsgResp.ReplyMsg replyMsgVO = new TextMsgResp.ReplyMsg();
            replyMsgVO.setId(replyMessage.getId());
            replyMsgVO.setUid(replyMessage.getFromUid());
            replyMsgVO.setType(replyMessage.getType());
            replyMsgVO.setBody(MsgHandlerFactory.getStrategyNoNull(replyMessage.getType()).showReplyMsg(replyMessage));
            User replyUser = userCache.getUserInfo(replyMessage.getFromUid());
            replyMsgVO.setUsername(replyUser.getName());
            replyMsgVO.setCanCallback(YesOrNoEnum.toStatus(Objects.nonNull(msg.getGapCount()) && msg.getGapCount() <= MessageAdapter.CAN_CALLBACK_GAP_COUNT));
            replyMsgVO.setGapCount(msg.getGapCount());
            resp.setReply(replyMsgVO);
        }
        return resp;
    }

    @Override
    public Object showReplyMsg(Message msg) {
        return msg.getContent();
    }

    @Override
    public String showContactMsg(Message msg) {
        return msg.getContent();
    }
}
