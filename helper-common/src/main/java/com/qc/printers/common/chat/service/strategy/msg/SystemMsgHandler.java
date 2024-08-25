package com.qc.printers.common.chat.service.strategy.msg;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import com.qc.printers.common.chat.dao.MessageDao;
import com.qc.printers.common.chat.domain.entity.Message;
import com.qc.printers.common.chat.domain.entity.msg.MessageExtra;
import com.qc.printers.common.chat.domain.enums.MessageTypeEnum;
import com.qc.printers.common.chat.domain.vo.request.ChatMessageReq;
import com.qc.printers.common.chat.domain.vo.request.msg.TextMsgReq;
import com.qc.printers.common.common.utils.discover.PrioritizedUrlDiscover;
import com.qc.printers.common.common.utils.discover.domain.UrlInfo;
import com.qc.printers.common.common.utils.sensitiveWord.SensitiveWordBs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;


/**
 * Description:系统消息
 * 修复ext不存在
 * 正常系统消息确实不可能存在回复，但是为了完整性还是补上了
 * Author: <a href="https://github.com/zongzibinbin">abin</a>
 * Date: 2023-06-04
 */
@Component
public class SystemMsgHandler extends AbstractMsgHandler {


    private static final PrioritizedUrlDiscover URL_TITLE_DISCOVER = new PrioritizedUrlDiscover();

    @Autowired
    private SensitiveWordBs sensitiveWordBs;

    @Autowired
    private MessageDao messageDao;

    @Override
    MessageTypeEnum getMsgTypeEnum() {
        return MessageTypeEnum.SYSTEM;
    }

    @Override
    public void checkMsg(ChatMessageReq request, Long uid) {
    }

    @Override
    public void saveMsg(Message msg, ChatMessageReq request) {
        TextMsgReq body = BeanUtil.toBean(request.getBody(), TextMsgReq.class);
        Message update = new Message();
        MessageExtra extra = Optional.ofNullable(msg.getExtra()).orElse(new MessageExtra());
        update.setExtra(extra);
        update.setId(msg.getId());
        update.setContent(sensitiveWordBs.filter(body.getContent()));
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
        return msg.getContent();
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
