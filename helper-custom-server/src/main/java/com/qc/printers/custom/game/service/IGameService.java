package com.qc.printers.custom.game.service;

import com.qc.printers.common.game.domain.entity.Game;
import java.util.List;

public interface IGameService {
    Game saveGame(Game game);
    List<Game> getSaves(String gameType);
    Game loadSave(Long id);
    String deleteSave(Long id);

    List<Game> getLeaderboard(String gameType);
}
