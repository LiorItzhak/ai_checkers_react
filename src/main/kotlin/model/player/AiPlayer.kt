package model.player

import kotlinx.coroutines.delay
import model.Board
import model.Piece
import model.algorithm.BoardGameNode
import model.algorithm.GameTreeAlgo
import model.game.Checkers.CheckersGame
import model.game.Checkers.CheckersMove
import model.game.Checkers.Move
import model.game.BoardGame

abstract class AiPlayer<G:BoardGame<M,out Board<out Piece>>,M:Move>(name: String): Player<G,M>(name)


class CheckerRandomPlayer(name:String?=null): AiPlayer<CheckersGame,CheckersMove>(name?:"random Player") {

    override suspend fun calcMove(game: CheckersGame, backupMove: CommittedMove<CheckersMove>): CheckersMove {
        delay(3000)
        return game.getRandomMove(player)
    }

}

class CheckersAiPlayer(val algo: GameTreeAlgo<CheckersMove>) : AiPlayer<CheckersGame, CheckersMove>("MinMaxPlayer") {

    override suspend fun calcMove(game: CheckersGame, backupMove: CommittedMove<CheckersMove>): CheckersMove {
        return algo.getBestMove(BoardGameNode(game, player))
    }

}


