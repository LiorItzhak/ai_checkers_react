package model.player

import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeout
import model.Board
import model.Piece
import model.algorithm.*
import model.game.Checkers.CheckersGame
import model.game.Checkers.CheckersMove
import model.game.Checkers.Move
import model.game.BoardGame

abstract class AiPlayer<G : BoardGame<M, out Board<out Piece>>, M : Move>(name: String) : Player<G, M>(name)


class CheckerRandomPlayer(name: String? = null) : AiPlayer<CheckersGame, CheckersMove>(name ?: "random Player") {

    override suspend fun calcMove(game: CheckersGame, backupMove: CommittedMove<CheckersMove>): CheckersMove {
        delay(2000)
        return game.getRandomMove(player)
    }

}

class CheckersAiPlayer(private val algo: GameTreeAlgo<CheckersMove>? = null,
                       private val time: Long? = null) : AiPlayer<CheckersGame, CheckersMove>("AlphaBetaPlayer") {

    override suspend fun calcMove(game: CheckersGame, backupMove: CommittedMove<CheckersMove>): CheckersMove {
        if (game.getAllPossibleMoves(player).size == 1)
            return game.getAllPossibleMoves(player).first()
        val node = BoardGameNode(game, player)

        if (algo != null && time == null)
            return algo.getBestMove(node)
        else if (time == null) {
            var i = 1
            while (true) {
                console.log("alpha beta depth is: $i")
                delay(1)
                backupMove.commit(AlphaBetaAlgo<CheckersMove>(i++).getBestMove(node))
            }
            TODO()
        } else {
            withTimeout(time) {
                var i = 1
                while (true) {
                    console.log("alpha beta depth is: $i")
                    delay(1)
                    backupMove.commit(AlphaBetaAlgo<CheckersMove>(i++).getBestMove(node))
                }
                TODO()
            }
        }
    }

}


class CheckersMctsAiPlayer(private val maxIterations: Int? = null, private val maxDepth: Int? = null) : AiPlayer<CheckersGame, CheckersMove>("MCTS Player") {
    private val mcts = MonteCarloTreeSearch<CheckersStaticState>()
    override suspend fun calcMove(game: CheckersGame, backupMove: CommittedMove<CheckersMove>): CheckersMove {
        val rootState = CheckersStaticState(game, player)
        return mcts.search(rootState, maxIterations, maxDepth) { backupMove.commit(it.move!!) }.move!!
    }


    class CheckersStaticState(val game: CheckersGame, val player: BoardGame.Player, val move: CheckersMove? = null) : StaticState {
        private val opponent = if (player == BoardGame.Player.Player1) BoardGame.Player.Player2 else BoardGame.Player.Player1
        private val children: List<CheckersStaticState> by lazy {
            game.getAllPossibleMoves(player).map { CheckersStaticState(game.copy().apply { applyMove(it) }, opponent, it) }
        }

        override val isTerminal: Boolean
            get() = children.isEmpty()

        override fun evaluate(): Double = game.getScore(player) / 50.0

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
            return player == other.player && other.game.board == game.board //not check move because root state dont have move (and it may be equals to another state
        }

        override fun hashCode(): Int {
            var result = game.board.hashCode()
            result = 31 * result + player.hashCode()
            result = 31 * result + (move?.hashCode() ?: 0)
            return result
        }
    }


}

