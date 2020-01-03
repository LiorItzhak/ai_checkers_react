package chekers.model.players

import boradGames.algorithm.AlphaBetaAlgo
import boradGames.algorithm.BoardGameNode
import chekers.model.game.CheckersGame
import chekers.model.game.CheckersMove
import boradGames.players.AiPlayer
import kotlinx.coroutines.delay

class CheckersAlphaBetaPlayer(private val maxDepth: Int? = null) : AiPlayer<CheckersGame, CheckersMove>("AlphaBetaPlayer") {
    private val algo = AlphaBetaAlgo<CheckersMove>()

    override suspend fun calcMove(game: CheckersGame, backupMove: CommittedMove<CheckersMove>): CheckersMove {
        algo.disposeCache()
        if (game.possibleMoves().size == 1)
            return game.possibleMoves().first()
        val rootNode = BoardGameNode(game, player)
        try {
            var depth = 1
            var move: CheckersMove
            do {
                move = algo.getBestMove(rootNode, depth++).also { backupMove.commit(it) }
                delay(1) //only for single threaded endearment - allows context switch
            } while (maxDepth == null || depth <= maxDepth)
            return move
        } finally {
            algo.disposeCache()
            console.log("cache disposed")
        }
    }

}