package boradGames.game.board

import boradGames.game.board.pieces.Piece
import boradGames.game.Move

abstract class BoardGame<M: Move,B : Board<out Piece>>(open val board: B) {

    var currentPlayer: Player = Player.Player1
        protected  set
    abstract fun initBoard()
    //throw illegalMoveException if the move is illegal
    //if multiMoveDelay is not null then play the moves with delay between the moves' parts
    abstract  fun applyMove(move: M)

    //throw exception if no move available
    abstract fun getRandomMove(player: Player): M

    abstract fun possibleMoves(): List<M>


    abstract fun isEnded(): Boolean

    abstract fun getScore(player: Player): Double

    abstract fun copy(): BoardGame<M, B>

    enum class Player {
        Player1 {
            override fun getOpponent() = Player2
        },
        Player2 {
            override fun getOpponent() = Player1
        };

        abstract fun getOpponent(): Player
    }

    abstract override fun equals(other: Any?): Boolean

    abstract override fun hashCode(): Int
}