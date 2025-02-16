package com.qc.printers.custom.game.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

import com.qc.printers.common.common.CustomException;
import com.qc.printers.common.common.utils.ThreadLocalUtil;
import com.qc.printers.common.game.dao.GameDao;
import com.qc.printers.common.game.domain.entity.Game;
import com.qc.printers.common.user.domain.dto.UserInfo;
import com.qc.printers.custom.game.service.IGameService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Slf4j
@Service
public class GameServiceImpl implements IGameService {
    @Autowired
    private GameDao gameDao;
    
    @Override
    public Game saveGame(Game gameSave) {
        UserInfo currentUser = ThreadLocalUtil.getCurrentUser();
        gameSave.setUserId(currentUser.getId());
        gameDao.saveOrUpdate(gameSave);
        Game byId = gameDao.getById(gameSave.getId());
        log.info("保存游戏成功:{},json:{}", byId,byId.getGameData());

        return byId;
    }

    @Override
    public List<Game> getSaves(String gameType) {
        UserInfo currentUser = ThreadLocalUtil.getCurrentUser();
        if (currentUser == null) {
           throw new CustomException("用户未登录");
        }
        LambdaQueryWrapper<Game> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Game::getGameType, gameType)
               .eq(Game::getUserId, currentUser.getId())
               .orderByDesc(Game::getCreateTime);
        return gameDao.list(wrapper);
    }

    @Override
    public Game loadSave(Long id) {
        return gameDao.getById(id);
    }

    @Override
    public String deleteSave(Long id) {
        gameDao.removeById(id);
        return "删除成功";
    }

    @Override
    public List<Game> getLeaderboard(String gameType) {
        // 实现排行榜，返回每个用户最大得分的一个记录，从分数从大到小排序
        LambdaQueryWrapper<Game> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Game::getGameType, gameType)
               .groupBy(Game::getUserId)
               .orderByDesc(Game::getScore);
        return gameDao.list(wrapper);
    }
}
