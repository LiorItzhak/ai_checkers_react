package model.player

import kotlinx.coroutines.delay
import model.Board
import model.Piece
import model.algorithm.BoardGameNode
import model.algorithm.GameTreeAlgo
import model.algorithm.MonteCarloTreeSearch
import model.algorithm.StaticState
import model.game.Checkers.CheckersGame
import model.game.Checkers.CheckersMove
import model.game.Checkers.Move
import model.game.BoardGame

abstract class AiPlayer<G : BoardGame<M, out Board<out Piece>>, M : Move>(name: String) : Player<G, M>(name)


class CheckerRandomPlayer(name: String? = null) : AiPlayer<CheckersGame, CheckersMove>(name ?: "random Player") {

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


class CheckersMctsAiPlayer : AiPlayer<CheckersGame, CheckersMove>("MCTS Player") {

    override suspend fun calcMove(game: CheckersGame, backupMove: CommittedMove<CheckersMove>): CheckersMove {
        val mcts = MonteCarloTreeSearch<CheckersStaticState>()
        val rootState = CheckersStaticState(game, player)
        return mcts.search(rootState,maxIterations = 5000, maxDepth = 20){ backupMove.commit(it.move!!) }.move!!
    }

    class CheckersStaticState(val game: CheckersGame, val player: BoardGame.Player, val move: CheckersMove? = null) : StaticState {
        private val opponent = if (player == BoardGame.Player.Player1) BoardGame.Player.Player2 else BoardGame.Player.Player1
        private val children: List<CheckersStaticState> by lazy {
            game.getAllPossibleMoves(player).map { CheckersStaticState(game.copy().apply { applyMove(it) }, opponent, it) }
        }

        override val isTerminal: Boolean
            get() = children.isEmpty()

        override fun evaluate(): Double = game.getScore(player).toDouble()

        override fun getChildren(): List<StaticState> = children

        override val perspective: Int
            get() =when(player){
                BoardGame.Player.Player1->1
                BoardGame.Player.Player2->2
            }

    }
}

