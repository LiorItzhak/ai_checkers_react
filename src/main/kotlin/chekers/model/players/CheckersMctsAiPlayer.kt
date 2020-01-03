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
    private val mcts = MonteCarloTreeSearch<CheckersStaticState>(cacheStates = true, usePreviousSearchInfo = true, ucb1Alpha = 1.41, logger = { console.log(it) })
    override suspend fun calcMove(game: CheckersGame, backupMove: CommittedMove<CheckersMove>): CheckersMove {
        val rootState = CheckersStaticState(game)
        return mcts.search(rootState, maxIterations, maxDepth) { backupMove.commit(it.move!!) }.move!!
    }


    /**
     * This inner class is representing a static state of a checkers game
     * The MonteCarloTreeSearch use this state to create a tree search
     * @param game contains the current state of the game
     * @param move contains the last previous move that played to create this state
     * note that [move] is null only for the root state
     */
    inner class CheckersStaticState(val game: CheckersGame, val move: CheckersMove? = null) : StaticState {

        private val children: List<CheckersStaticState> by lazy {
            game.possibleMoves().map { CheckersStaticState(game.copy().apply { applyMove(it) }, it) }
        }

        override val isTerminal: Boolean
            get() = game.isEnded()

        override fun evaluate(perspective: Int): Double = when (perspective) {
            1 -> game.getScoreMcts(BoardGame.Player.Player1)
            2 -> game.getScoreMcts(BoardGame.Player.Player2)
            else -> throw IllegalStateException("Player perspective evaluation $perspective dont implemented")
        }


        override fun getChildren(): List<StaticState> = children

        override val perspective: Int
            get() = when (game.currentPlayer) {
                BoardGame.Player.Player1 -> 1
                BoardGame.Player.Player2 -> 2
            }


        override fun toString(): String = move.toString()

        override fun equals(other: Any?): Boolean {
            if (other !is CheckersStaticState)
                return false
            //dont check move because root state dont have a move (and it may be equals to another state)
            return other.game == game
        }

        override fun hashCode(): Int = game.hashCode()

        /**
         * Very basic evaluation function for checkers game
         * normalized scores in range [-1,1]
         * draw = score 0.0
         * [player] win = 1.0
         * [player] lose = -1.0
         */
        private fun CheckersGame.getScoreMcts(player: BoardGame.Player): Double {

            fun getWinner() : BoardGame.Player? {
                if (!isEnded() || drawStepCounter >= 15)
                    return null //draw or not ended
                if(possibleMoves().isEmpty())
                    return currentPlayer.getOpponent()
                return currentPlayer
            }

            if (isEnded()) {
               val winner = getWinner() ?: return 0.0 //draw
                return if (player == winner) 1.0 else -1.0
            }


            fun toolsScore(player: BoardGame.Player): Double {
                var score = 0.0
                cartesianFor(CheckersGame.BOARD_SIZE, CheckersGame.BOARD_SIZE) { pos ->
                    board[pos]?.let {
                        when (it) {
                            is Queen -> if (it.owner == player) score += 15 else score -= 15
                            is King -> if (it.owner == player) score += 8 else score -= 8
                            is RegularPiece -> if (it.owner == player) score += 4 else score -= 4
                        }
                    }
                }
                return score / board.size* board.size*1.5
            }
            return toolsScore(player)
        }
    }


}
