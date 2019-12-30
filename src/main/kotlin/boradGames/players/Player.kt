package boradGames.players

import kotlinx.coroutines.*
import boradGames.game.board.Board
import boradGames.game.board.pieces.Piece
import boradGames.game.board.BoardGame
import boradGames.game.Move

abstract class Player<T : BoardGame<M, out Board<out Piece>>, M : Move>(val name: String) {
    lateinit var player: BoardGame.Player

    var backupMove: CommittedMove<M> = CommittedMove()
    suspend fun startTurn(game: T): M? {
        backupMove = CommittedMove<M>()
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
        } catch (e: Throwable) {
            console.info("ERROR-$player--${e.message}-${e.cause}")
        } finally {
            //if the turn is canceled return the committed backup move
            return move

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





