package com.qc.printers.common.chat.dao;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qc.printers.common.chat.domain.entity.Contact;
import com.qc.printers.common.chat.domain.entity.Message;
import com.qc.printers.common.chat.mapper.ContactMapper;
import com.qc.printers.common.common.CustomException;
import com.qc.printers.common.common.domain.vo.request.CursorPageBaseReq;
import com.qc.printers.common.common.domain.vo.response.CursorPageBaseResp;
import com.qc.printers.common.common.utils.CursorUtils;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * <p>
 * 会话列表 服务实现类
 * </p>
 *
 * @author <a href="https://github.com/zongzibinbin">abin</a>
 * @since 2023-07-16
 */
@Service
public class ContactDao extends ServiceImpl<ContactMapper, Contact> {

    public Contact get(Long uid, Long roomId) {
        return lambdaQuery()
                .eq(Contact::getUid, uid)
                .eq(Contact::getRoomId, roomId)
                .one();
    }

    public Integer getReadCount(Message message) {
        return Math.toIntExact(lambdaQuery()
                .eq(Contact::getRoomId, message.getRoomId())
                .ne(Contact::getUid, message.getFromUid())//不需要查询出自己
                .ge(Contact::getReadTime, message.getCreateTime())
                .count());
    }

    public Integer getTotalCount(Long roomId) {
        return Math.toIntExact(lambdaQuery()
                .eq(Contact::getRoomId, roomId)
                .count());
    }

    public Integer getUnReadCount(Message message) {
        return Math.toIntExact(lambdaQuery()
                .eq(Contact::getRoomId, message.getRoomId())
                .lt(Contact::getReadTime, message.getCreateTime())
                .count());
    }

    public CursorPageBaseResp<Contact> getReadPage(Message message, CursorPageBaseReq cursorPageBaseReq) {
        return CursorUtils.getCursorPageByMysql(this, cursorPageBaseReq, wrapper -> {
            wrapper.eq(Contact::getRoomId, message.getRoomId());
            wrapper.ne(Contact::getUid, message.getFromUid());//不需要查询出自己
            wrapper.ge(Contact::getReadTime, message.getCreateTime());//已读时间大于等于消息发送时间
            wrapper.orderByDesc(Contact::getReadTime);
        }, Contact::getReadTime);
    }

    public CursorPageBaseResp<Contact> getUnReadPage(Message message, CursorPageBaseReq cursorPageBaseReq) {
        return CursorUtils.getCursorPageByMysql(this, cursorPageBaseReq, wrapper -> {
            wrapper.eq(Contact::getRoomId, message.getRoomId());
            wrapper.ne(Contact::getUid, message.getFromUid());//不需要查询出自己
            wrapper.lt(Contact::getReadTime, message.getCreateTime());//已读时间小于消息发送时间
            wrapper.orderByDesc(Contact::getReadTime);//已读时间小于消息发送时间
        }, Contact::getReadTime);
    }

    /**
     * 获取用户会话列表
     */
    public CursorPageBaseResp<Contact> getContactPage(Long uid, CursorPageBaseReq request) {
        return CursorUtils.getCursorPageByMysql(this, request, wrapper -> {
            wrapper.eq(Contact::getUid, uid);
            wrapper.orderByDesc(Contact::getActiveTime);
        }, Contact::getActiveTime);
    }

    public List<Contact> getByRoomIds(List<Long> roomIds, Long uid) {
        return lambdaQuery()
                .eq(Contact::getRoomId, roomIds)
                .eq(Contact::getUid, uid)
                .list();
    }

    /**
     * 更新所有人的会话时间，没有就直接插入
     */
    public void refreshOrCreateActiveTime(Long roomId, List<Long> memberUidList, Long msgId, Date activeTime) {
        baseMapper.refreshOrCreateActiveTime(roomId, memberUidList, msgId, activeTime);
    }

    /**
     * 根据房间ID删除会话
     *
     * @param roomId  房间ID
     * @param uidList 群成员列表
     * @return 是否删除成功
     */
    public Boolean removeByRoomId(Long roomId, List<Long> uidList) {
        if (uidList==null){
            throw new CustomException("异常参数");
        }
        LambdaQueryWrapper<Contact> wrapper = new QueryWrapper<Contact>().lambda()
                .eq(Contact::getRoomId, roomId)
                .in(uidList.size()>0,Contact::getUid, uidList);
        return this.remove(wrapper);

    }
}
