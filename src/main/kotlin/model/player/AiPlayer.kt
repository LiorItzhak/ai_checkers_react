package model.player

import cartesianFor
import kotlinx.coroutines.delay
import model.Board
import model.Piece
import model.algorithm.*
import model.game.Checkers.CheckersGame
import model.game.Checkers.CheckersMove
import model.game.Checkers.Move
import model.game.BoardGame
import model.game.Checkers.pieces.King
import model.game.Checkers.pieces.Queen
import model.game.Checkers.pieces.RegularPiece

abstract class AiPlayer<G : BoardGame<M, out Board<out Piece>>, M : Move>(name: String) : Player<G, M>(name)


class CheckerRandomPlayer(name: String? = null) : AiPlayer<CheckersGame, CheckersMove>(name ?: "random Player") {

    override suspend fun calcMove(game: CheckersGame, backupMove: CommittedMove<CheckersMove>): CheckersMove {
        delay(2000)
        return game.getRandomMove(player)
    }

}

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
            TODO()
        } finally {
            console.log("cache disposed")
            algo.disposeCache()
        }
    }
}


class CheckersMctsAiPlayer(private val maxIterations: Int? = null, private val maxDepth: Int? = null) : AiPlayer<CheckersGame, CheckersMove>("MCTS Player") {
    private val mcts = MonteCarloTreeSearch<CheckersStaticState>(cacheStates = true,usePreviousSearchInfo = true,ucb1Alpha =1.41)
    override suspend fun calcMove(game: CheckersGame, backupMove: CommittedMove<CheckersMove>): CheckersMove {
        val rootState = CheckersStaticState(game, player)
        return mcts.search(rootState, maxIterations, maxDepth) { backupMove.commit(it.move!!) }.move!!
    }


    inner class CheckersStaticState(val game: CheckersGame, val player: BoardGame.Player, val move: CheckersMove? = null) : StaticState {
        private val children: List<CheckersStaticState> by lazy {
            game.possibleMoves().map { CheckersStaticState(game.copy().apply { applyMove(it) },  player.getOpponent(), it) }
        }

        override val isTerminal: Boolean
            get() = game.isEnded()

        override fun evaluate(perspective: Int): Double = when (perspective) {
            1 -> game.getScoreMcts(BoardGame.Player.Player1)
            2 -> game.getScoreMcts(BoardGame.Player.Player2)
            else -> TODO("Player perspective evaluation $perspective dont implemented")
        }


        override fun getChildren(): List<StaticState> = children

        override val perspective: Int
            get() = when (player) {
                BoardGame.Player.Player1 -> 1
                BoardGame.Player.Player2 -> 2
            }

        override fun toString(): String {
            return move.toString()
        }


        override fun equals(other: Any?): Boolean {
            if (other !is CheckersStaticState)
                return false
            return other.game == game //not check move because root state dont have move (and it may be equals to another state
        }

        override fun hashCode(): Int {
            return game.hashCode()
        }


        private fun CheckersGame.getScoreMcts(player: BoardGame.Player): Double {
          //  fun getDistFromEnd(pos: Pair<Int, Int>, owner: BoardGame.Player): Double = (if (owner == BoardGame.Player.Player2) pos.first else CheckersGame.BOARD_SIZE - 1 - pos.first).toDouble() / CheckersGame.BOARD_SIZE
            if (drawStepCounter >= 15) return 0.5//draw
            if(game.isEnded()) return if(player == currentPlayer) 0.0 else 1.0

            var score = 50.0
            cartesianFor(CheckersGame.BOARD_SIZE, CheckersGame.BOARD_SIZE) { pos ->
                board[pos]?.let {
                    when (it) {
                        is RegularPiece -> if (it.owner == player) score +=1 else score -= 1
                        is King -> if (it.owner == player) score += 3 else score-=4
                        is Queen -> if (it.owner == player) score += 5 else score -=7
                    }
                }
            }
            return score/100.0 //normalize 0 to 1.0
        }
    }


}

