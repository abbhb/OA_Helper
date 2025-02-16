package com.qc.printers.custom.game.controller;

import com.qc.printers.common.common.R;
import com.qc.printers.common.common.annotation.NeedToken;
import com.qc.printers.common.game.domain.entity.Game;
import com.qc.printers.custom.game.service.IGameService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/game")
@Slf4j
@CrossOrigin("*")
@Api("Game相关接口")
public class GameController {
    
    @Autowired
    private IGameService gameService;

    @PostMapping("/save")
    @NeedToken
    @ApiOperation("保存游戏")
    public R<Game> saveGame(@RequestBody Game gameSave) {
        return R.success(gameService.saveGame(gameSave));
    }

    @NeedToken
    @GetMapping("/saves")
    @ApiOperation("获取游戏存档列表")
    public R<List<Game>> getSaves(@RequestParam String gameType) {
        return R.success(gameService.getSaves(gameType));
    }

    @NeedToken
    @GetMapping("/save/{id}")
    @ApiOperation("加载游戏存档")
    public R<Game> loadSave(@PathVariable Long id) {
        return R.success(gameService.loadSave(id));
    }
    @NeedToken
    @GetMapping("/s/{gameType}")
    @ApiOperation("加载游戏存档")
    public R<List<Game>> getLeaderboard(@PathVariable String gameType) {
        return R.success(gameService.getLeaderboard(gameType));
    }

    @NeedToken
    @DeleteMapping("/save/{id}")
    @ApiOperation("删除游戏存档")
    public R<String> deleteSave(@PathVariable Long id) {
        return R.success(gameService.deleteSave(id));
    }
}
