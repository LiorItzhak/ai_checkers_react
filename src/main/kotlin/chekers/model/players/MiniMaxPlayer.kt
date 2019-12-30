package chekers.model.players

import kotlinx.coroutines.delay
import boradGames.algorithm.AlphaBetaAlgo
import boradGames.algorithm.BoardGameNode
import boradGames.algorithm.GameTreeAlgo
import chekers.model.game.CheckersGame
import chekers.model.game.CheckersMove
import boradGames.players.AiPlayer

class CheckersAiPlayer(algo: GameTreeAlgo<CheckersMove>? = null)
    : AiPlayer<CheckersGame, CheckersMove>("AlphaBetaPlayer") {
    private val algo = algo ?: AlphaBetaAlgo()

    //TODO add depth limit
    override suspend fun calcMove(game: CheckersGame, backupMove: CommittedMove<CheckersMove>): CheckersMove {
        algo.disposeCache()
        if (game.possibleMoves().size == 1)
            return game.possibleMoves().first()
        val node = BoardGameNode(game, player)

        try {
            var i = 1
            while (true) {
                delay(1)
                backupMove.commit(algo.getBestMove(node, i++))
            }
        } finally {
            console.log("cache disposed")
            algo.disposeCache()
        }
    }
}