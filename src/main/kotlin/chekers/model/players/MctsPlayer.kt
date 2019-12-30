package chekers.model.players

import cartesianFor
import boradGames.algorithm.MonteCarloTreeSearch
import boradGames.algorithm.StaticState
import boradGames.game.board.BoardGame
import chekers.model.game.CheckersGame
import chekers.model.game.CheckersMove
import chekers.model.game.board.pieces.King
import chekers.model.game.board.pieces.Queen
import chekers.model.game.board.pieces.RegularPiece
import boradGames.players.AiPlayer

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
            if (drawStepCounter >= 15) return 0.0//0.5//draw

            fun toolsScore(player: BoardGame.Player):Double{
                var score = 0.0
                cartesianFor(CheckersGame.BOARD_SIZE, CheckersGame.BOARD_SIZE) { pos ->
                    board[pos]?.let {
                        when (it) {
                            is Queen -> if (it.owner == player) score += 15 else score -=15
                            is King -> if (it.owner == player) score += 8 else score-=8
                            is RegularPiece -> if (it.owner == player) score += 4 else score -= 4

                        }
                    }
                }
                return score/100.0
            }

            if(game.isEnded()) {
                val winLoseScore = if(player == currentPlayer) -1.0 else 1.0 //0 to 1
                return  winLoseScore  //*0.8  +toolsScore(player)*0.2
            }

            return  toolsScore(player)
        }
    }


}
