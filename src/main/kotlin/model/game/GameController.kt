package model.game

import kotlinx.coroutines.*
import model.Board
import model.Piece
import model.game.Checkers.Move
import model.player.HumanPlayer
import model.player.Player
import org.w3c.dom.Worker
import kotlin.browser.window
import kotlin.js.Date


interface IGameController {
    fun getTurn(): BoardGame.Player
}

class GameController<T : BoardGame<M, B>, B : Board<out Piece>, M : Move>(
        private val player1: Player<T, M>,
        private val player2: Player<T, M>,
        private val game: T,
        private val timeLimitMillis: Long? = null) : IGameController {
    private val listeners = mutableListOf<IGameControllerListener<T, B>>()
    fun addListener(listener: IGameControllerListener<T, B>) = listeners.add(listener)
    fun removeListener(listener: IGameControllerListener<T, B>) = listeners.remove(listener)

    private var turnNum = 0
    private var currentPlayer = player1

    private val turn
        get() = if (currentPlayer == player1) BoardGame.Player.Player1 else BoardGame.Player.Player2

    suspend fun startNewGame() {
        console.info("Start new game")
        initGame()
        delay(1000)
        idle()
        endGame()
    }

    private fun initGame() {
        game.initBoard()
        console.info("game initialized")
        listeners.forEach { it.onBoardChanged(game.board.copy() as B) } //notify board has change
        currentPlayer = player1
        player1.player = BoardGame.Player.Player1
        player2.player = BoardGame.Player.Player2
    }


    private fun endGame() {
        console.info("end game")
        val player1Score = game.getScore(player1.player)
        val player2Score = game.getScore(player2.player)
        val winner = when (game.isGameEnded(turn)) {
            true -> if (player1Score > player2Score) player1.player else if (player1Score < player2Score) player2.player else null
            else -> null
        }
        listeners.forEach { it.onGameEnded(winner, game.getScore(winner ?: player1.player)) }
    }


    //game loop
    private suspend fun idle() {
        do {
            turnNum++
            val startTime = Date()
            console.info("${currentPlayer.name} : start turn${turnNum} -${startTime.toTimeString()}")
            listeners.forEach { it.onTurnStarted(game.copy() as T, currentPlayer.player) }//notify - turn is started
            //TODO pass the player a copy of the game, by do so he will be unable to cheat.
            //if there is a time limit then start the turn with timout
            var move: M? = when (timeLimitMillis) {
                null -> currentPlayer.startTurn(game.copy() as T)
                else -> {
                    listeners.forEach { it.onTimeoutTimerStart(timeLimitMillis) }//notify - timeLimitMillis is started
                    val m = withTimeoutOrNull(timeLimitMillis) { currentPlayer.startTurn(game.copy() as T) }
                    listeners.forEach { it.onTimeoutTimerEnd(timeLimitMillis) }//notify - timeLimitMillis is started
                    m
                }
            }
            when (move) {
                null -> console.info("${currentPlayer.name} : timeout, generate random move")
                else -> console.info("${currentPlayer.name} : calculated move : $move")
            }
            //if no move as given (turn timeout) generate a random move
            move = move ?: game.getRandomMove(turn)
            console.log("move: $move")

            if (!game.getAllPossibleMoves(turn).any { it == move }) {
                console.error("${currentPlayer.player} played illegal move :$move, ending game")
                break
            }

            //apply the turn
            listeners.forEach { it.onMoveDecided(move as Move, game.board.copy() as B) }
            listeners.forEach { it.playMoveAnimation(game.copy() as T, move) }//notify board has change
            game.applyMove(move)
            listeners.forEach { it.onBoardChanged(game.board.copy() as B) }//notify board has change

            //toggle turn
            val endTime = Date()
            console.info("${currentPlayer.name} : end turn${turnNum} -${endTime.toTimeString()} ---- ${(endTime - startTime).toTimeString()}  ")
            console.info("${currentPlayer.name} : end turn")
            listeners.forEach { it.onTurnEnded(game.copy() as T, currentPlayer.player) }//notify - turn is ended
            delay(500)
            currentPlayer = if (currentPlayer == player1) player2 else player1
        } while (!game.isGameEnded(currentPlayer.player))


    }


    interface IGameControllerListener<T : BoardGame<out Move, B>, B : Board<out Piece>> {
        fun onBoardChanged(board: B)

        fun onMoveDecided(move: Move, board: B)

        fun onTurnStarted(game: T, turn: BoardGame.Player)

        fun onTurnEnded(game: T, turn: BoardGame.Player)

        fun onScoreChanged(player1Score: Int, player2Score: Int)

        fun onGameEnded(winner: BoardGame.Player?, score: Int)

        fun onTimeoutTimerStart(timeoutMillis: Long)

        fun onTimeoutTimerEnd(timeoutMillis: Long)

        suspend fun playMoveAnimation(game: T, move: Move)

    }

    override fun getTurn(): BoardGame.Player = turn


}

private operator fun Date.minus(startTime: Date) = Date(
        year = this.getFullYear() - startTime.getFullYear(),
        month = this.getMonth() - startTime.getMonth(),
        day = this.getDay() - startTime.getDay(),
        hour = this.getHours() - startTime.getHours(),
        minute = this.getMinutes() - startTime.getMinutes(),
        second = this.getSeconds() - startTime.getSeconds(),
        millisecond = this.getMilliseconds() - startTime.getMilliseconds()

)
