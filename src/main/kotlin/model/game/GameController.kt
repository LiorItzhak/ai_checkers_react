package model.game

import kotlinx.coroutines.*
import model.Board
import model.Piece
import model.game.Checkers.Move
import model.player.HumanPlayer
import model.player.Player
import kotlin.js.Date


val MULTIMOVE_DELAY = 1000L

interface IGameController {
    fun getTurn(): BoardGame.Player
}

class GameController<T : BoardGame<M, B>, B : Board<out Piece>, M : Move>(
        private val player1: Player<T, M>,
        private val player2: Player<T, M>,
        private val game: T,
        private val timeLimitMillis: Long? = null) : IGameController {
    private val listeners = mutableListOf<IGameControllerListener<B>>()
    fun addListener(listener: IGameControllerListener<B>) = listeners.add(listener)
    fun removeListener(listener: IGameControllerListener<B>) = listeners.remove(listener)

    private var turnNum = 0
    private var currentPlayer = player1

    private val turn
        get() = if (currentPlayer == player1) BoardGame.Player.Player1 else BoardGame.Player.Player2

    suspend fun startNewGame() {
        console.info("Start new game")
        initGame()
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
            true -> if (player1Score > player2Score) player2.player else if (player1Score < player2Score) player2.player else null
            else -> null
        }
        listeners.forEach { it.onGameEnded(winner, game.getScore(winner ?: player1.player)) }
    }


    //game loop
    private suspend fun idle() {
        while (!game.isGameEnded(turn)) {
            console.info("${currentPlayer.name} : start turn${++turnNum} -${Date().toTimeString()}")
            listeners.forEach { it.onTurnStarted(currentPlayer.player) }//notify - turn is started
            //TODO pass the player a copy of the game, by do so he will be unable to cheat.
            //if there is a time limit then start the turn with timout
            val move: M? = when (timeLimitMillis) {
                null -> currentPlayer.startTurn(game.copy() as T)
                else -> withTimeoutOrNull(timeLimitMillis) { currentPlayer.startTurn(game.copy() as T) }
            }
            console.log("move: $move")
            when (move) {
                null -> console.info("${currentPlayer.name} : timeout, generate random move")
                else -> console.info("${currentPlayer.name} : calculated move : $move")
            }
            if (!game.getAllPossibleMoves(turn).any { it == move }) {
                console.error("${currentPlayer.player} played illegal move :$move, ending game")
                break
            }


            //apply the turn
            listeners.forEach { it.onMoveDecided(move as Move, game.board.copy() as B) }
            val multiMoveDelay = if (currentPlayer is HumanPlayer<*, *, *>) null else MULTIMOVE_DELAY
            game.applyMove((move ?: game.getRandomMove(turn)), multiMoveDelay)

            listeners.forEach { it.onBoardChanged(game.board.copy() as B) }//notify board has change

            //toggle turn
            console.info("${currentPlayer.name} : end turn")
            listeners.forEach { it.onTurnEnded(currentPlayer.player) }//notify - turn is ended
            currentPlayer = if (currentPlayer == player1) player2 else player1
        }
    }


    interface IGameControllerListener<B : Board<out Piece>> {
        fun onBoardChanged(board: B)

        fun onMoveDecided(move: Move, board: B)

        fun onTurnStarted(turn: BoardGame.Player)

        fun onTurnEnded(turn: BoardGame.Player)

        fun onScoreChanged(player1Score: Int, player2Score: Int)

        fun onGameEnded(winner: BoardGame.Player?, score: Int)

        fun onTimeoutTimerChanged(timeoutMillis: Long)
    }

    override fun getTurn(): BoardGame.Player = turn


}