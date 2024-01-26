package com.qc.printers.custom.user.service.impl;

import com.qc.printers.common.common.R;
import com.qc.printers.common.common.annotation.RedissonLock;
import com.qc.printers.common.common.domain.vo.response.IdRespVO;
import com.qc.printers.common.common.utils.AssertUtil;
import com.qc.printers.common.common.utils.oss.OssDBUtil;
import com.qc.printers.common.user.dao.UserEmojiDao;
import com.qc.printers.common.user.domain.entity.UserEmoji;
import com.qc.printers.common.user.domain.vo.request.user.UserEmojiReq;
import com.qc.printers.common.user.domain.vo.response.user.UserEmojiResp;
import com.qc.printers.custom.user.service.UserEmojiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户表情包 ServiceImpl
 *
 * @author: WuShiJie
 * @createTime: 2023/7/3 14:23
 */
@Service
@Slf4j
public class UserEmojiServiceImpl implements UserEmojiService {

    @Autowired
    private UserEmojiDao userEmojiDao;

    @Override
    public List<UserEmojiResp> list(Long uid) {
        return userEmojiDao.listByUid(uid).
                stream()
                .map(a -> UserEmojiResp.builder()
                        .id(a.getId())
                        .expressionUrl(OssDBUtil.toUseUrl(a.getExpressionUrl()))
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * 新增表情包
     *
     * @param uid 用户ID
     * @return 表情包
     * @author WuShiJie
     * @createTime 2023/7/3 14:46
     **/
    @Override
    @RedissonLock(key = "#uid")
    public R<IdRespVO> insert(UserEmojiReq req, Long uid) {
        //校验表情数量是否超过30
        int count = userEmojiDao.countByUid(uid);
        AssertUtil.isFalse(count > 30, "最多只能添加30个表情哦~~");
        String expressionUrlDB = OssDBUtil.toDBUrl(req.getExpressionUrl());
        //校验表情是否存在
        Integer existsCount = userEmojiDao.lambdaQuery()
                .eq(UserEmoji::getExpressionUrl,expressionUrlDB)
                .eq(UserEmoji::getUid, uid)
                .count();
        AssertUtil.isFalse(existsCount > 0, "当前表情已存在哦~~");
        UserEmoji insert = UserEmoji.builder().uid(uid).expressionUrl(expressionUrlDB).build();
        userEmojiDao.save(insert);
        return R.success(IdRespVO.id(insert.getId()));
    }

    @Override
    public void remove(Long id, Long uid) {
        UserEmoji userEmoji = userEmojiDao.getById(id);
        AssertUtil.isNotEmpty(userEmoji, "表情不能为空");
        AssertUtil.equal(userEmoji.getUid(), uid, "小黑子，别人表情不是你能删的");
        userEmojiDao.removeById(id);
    }
}
