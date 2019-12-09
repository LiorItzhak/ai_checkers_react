package model.game

import model.Board
import model.Piece
import model.game.Checkers.Move

abstract class BoardGame<M: Move,B : Board<out Piece>>(open val board: B) {


    abstract fun initBoard()
    //throw illegalMoveException if the move is illegal
    //if multiMoveDelay is not null then play the moves with delay between the moves' parts
    abstract suspend fun applyMove(move: M, multiMoveDelayMillis: Long? = null)

    //throw exception if no move available
    abstract fun getRandomMove(player: Player): M

    //throw exception if no move available
    abstract fun getAllPossibleMoves(player: Player): List<M>

    abstract fun isGameEnded(playerTurn: Player): Boolean

    abstract fun getScore(player: Player): Int

    abstract fun copy(): BoardGame<M, B>

    enum class Player { Player1, Player2 }

    protected val listeners = mutableListOf<IGameListener<B>>()
    fun addListener(listener: IGameListener<B>) = listeners.add(listener)
    fun removeListener(listener: IGameListener<B>) = listeners.remove(listener)
    interface IGameListener<B : Board<out Piece>>{
        fun onBoardChanged(board: B)
    }



}