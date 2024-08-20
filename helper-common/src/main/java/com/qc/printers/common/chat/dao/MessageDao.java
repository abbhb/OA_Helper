package com.qc.printers.common.chat.dao;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qc.printers.common.chat.domain.entity.Message;
import com.qc.printers.common.chat.domain.entity.MessageWithStateDto;
import com.qc.printers.common.chat.domain.enums.MessageStatusEnum;
import com.qc.printers.common.chat.mapper.MessageMapper;
import com.qc.printers.common.chat.mapper.MessageWithStateDtoMapper;
import com.qc.printers.common.common.CustomException;
import com.qc.printers.common.common.domain.vo.request.CursorPageBaseReq;
import com.qc.printers.common.common.domain.vo.response.CursorPageBaseResp;
import com.qc.printers.common.common.utils.CursorUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

import static com.qc.printers.common.common.utils.CursorUtils.parseCursor;

/**
 * <p>
 * 消息表 服务实现类
 * </p>
 *
 * @author <a href="https://github.com/zongzibinbin">abin</a>
 * @since 2023-03-25
 */
@Service
public class MessageDao extends ServiceImpl<MessageMapper, Message> {

    @Autowired
    private MessageWithStateDtoMapper messageWithStateDtoMapper;

    public CursorPageBaseResp<Message> getCursorPage(Long roomId, CursorPageBaseReq request, Long lastMsgId,Long uid) {
        // 特殊处理
//        return CursorUtils.getCursorPageByMysql(this, request, wrapper -> {
//            wrapper.apply("LEFT JOIN message_user_state musx ON musx.msg_id = message.id and musx.user_id = {0} AND (musx.state IS NULL OR musx.state = 0)",);
//
//        }, Message::getId);
        LambdaQueryWrapper<MessageWithStateDto> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MessageWithStateDto::getRoomId, roomId);
//        wrapper.eq(MessageWithStateDto::getUserId, uid);
        wrapper.eq(MessageWithStateDto::getStatus, MessageStatusEnum.NORMAL.getStatus());
        wrapper.le(Objects.nonNull(lastMsgId), MessageWithStateDto::getId, lastMsgId);

        if (StrUtil.isNotBlank(request.getCursor())) {
            wrapper.lt(MessageWithStateDto::getId, parseCursor(request.getCursor(), MessageWithStateDto.class));
        }

        wrapper.orderByDesc(MessageWithStateDto::getId);


        Page<MessageWithStateDto> page = messageWithStateDtoMapper.page(request.plusPage(), wrapper, uid);
        String cursor = Optional.ofNullable(CollectionUtil.getLast(page.getRecords()))
                .map(MessageWithStateDto::getId)
                .map(CursorUtils::toCursor)
                .orElse(null);
        Boolean isLast = page.getRecords().size() != request.getPageSize();
        List<Message> messages = new ArrayList<>();
        for (MessageWithStateDto record : page.getRecords()) {
            Message message = new Message();
            message.setId(record.getId());
            message.setExtra(record.getExtra());
            message.setContent(record.getContent());
            message.setType(record.getType());
            message.setFromUid(record.getFromUid());
            message.setStatus(record.getStatus());
            message.setCreateTime(record.getCreateTime());
            message.setGapCount(record.getGapCount());
            message.setRoomId(record.getRoomId());
            message.setUpdateTime(record.getUpdateTime());
            message.setReplyMsgId(record.getReplyMsgId());
            messages.add(message);
        }
        return new CursorPageBaseResp<>(cursor, isLast, messages);
    }

    /**
     * 乐观更新消息类型
     */
    public boolean riseOptimistic(Long id, Integer oldType, Integer newType) {
        return lambdaUpdate()
                .eq(Message::getId, id)
                .eq(Message::getType, oldType)
                .set(Message::getType, newType)
                .update();
    }

    public Integer getGapCount(Long roomId, Long fromId, Long toId) {
        return lambdaQuery()
                .eq(Message::getRoomId, roomId)
                .gt(Message::getId, fromId)
                .le(Message::getId, toId)
                .count();
    }

    public void invalidByUid(Long uid) {
        lambdaUpdate()
                .eq(Message::getFromUid, uid)
                .set(Message::getStatus, MessageStatusEnum.DELETE.getStatus())
                .update();
    }

    public Integer getUnReadCount(Long roomId, Date readTime) {
        return lambdaQuery()
                .eq(Message::getRoomId, roomId)
                .gt(Objects.nonNull(readTime), Message::getCreateTime, readTime)
                .count();
    }

    /**
     * 根据房间ID逻辑删除消息
     *
     * @param roomId  房间ID
     * @param uidList 群成员列表
     * @return 是否删除成功
     */
    public Boolean removeByRoomId(Long roomId, List<Long> uidList) {
        if (uidList==null){
            throw new CustomException("异常参数");
        }
        LambdaUpdateWrapper<Message> wrapper = new UpdateWrapper<Message>().lambda()
                .eq(Message::getRoomId, roomId)
                .in(uidList.size()>0,Message::getFromUid, uidList)
                .set(Message::getStatus, MessageStatusEnum.DELETE.getStatus());
        return this.update(wrapper);
    }
}
