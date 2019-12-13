package model.player

import kotlinx.coroutines.*
import model.Board
import model.Piece
import model.game.Checkers.Move
import model.game.BoardGame
import model.game.Checkers.CheckersGame
import model.game.Checkers.CheckersMove
import model.game.GameController
import kotlin.coroutines.coroutineContext

abstract class Player<T : BoardGame<M, out Board<out Piece>>, M : Move>(val name: String) {
    lateinit var player: BoardGame.Player

    suspend fun startTurn(game: T): M? {
        val backupMove = CommittedMove<M>()
        var move: M? = null
        //if the turn is cancelled then stop the move calculation and return the backup move
        try {
            //start the calculation on another job by do so we enforce the cancellation
            move = calcMove(game, backupMove)
        } catch (cancelE: CancellationException) {
            //the turn hes been cancelled!
            //cancel the move calculation - don't wait!, dont let the job delay the turn,
            // turnJob?.cancel()//dont need - shared context//TODO check this
            console.info("timeout $player--${name}----${cancelE.message}-")
        }
        catch (e:Throwable){
            console.info("ERROR-$player--${e.message}-")
        }finally {
            //if the turn is canceled return the committed backup move
            return withContext(NonCancellable) {
                return@withContext move ?: backupMove.take()
            }
        }
    }

    //the player can use CommittedMove to commit a backup move for timeout event
    protected abstract suspend fun calcMove(game: T, backupMove: CommittedMove<M>): M


     class CommittedMove<T : Move> {
        private var isTaken: Boolean = false
        private var move: T? = null

        fun commit(move: T): Boolean {
            val updatable = !isTaken
            if (updatable)
                this.move = move
            return updatable
        }

        internal fun take(): T? {
            isTaken = true
            console.info("debug: backup move taken $move")
            return move

        }
    }


}


abstract class CheckersPlayer(name: String) : Player<CheckersGame, CheckersMove>(name) {

}

