package com.qc.printers.common.game.dao;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qc.printers.common.game.domain.entity.Game;
import com.qc.printers.common.game.mapper.GameMapper;
import org.springframework.stereotype.Service;

@Service
public class GameDao extends ServiceImpl<GameMapper, Game> {
}
