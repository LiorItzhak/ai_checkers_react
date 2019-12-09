package viewmodel

import Utills.MutableObservable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import model.Piece
import model.algorithm.AlphaBetaAlgo
import model.game.BoardGame
import model.game.Checkers.CheckersBoard
import model.game.Checkers.CheckersGame
import model.game.Checkers.CheckersMove
import model.game.Checkers.Move
import model.game.Checkers.pieces.Queen
import model.game.Checkers.pieces.RegularPiece
import model.game.GameController
import model.game.IGameController
import model.player.CheckersAiPlayer
import model.player.HumanPlayer
import model.player.Player
import ui.Board
import ui.Square

//todo
const val URL_REG_PLAYER1 = "img/red-pawn.png"
const val URL_REG_PLAYER2 = "img/blue-pawn.png"
const val URL_QUEEN_PLAYER1 = "img/red-king.png"
const val URL_QUEEN_PLAYER2 = "img/blue-king.png"

const val WHITE_COLOR = "#f2f2f2"
const val BLACK_COLOR = "#262626"
const val PLAYER1_HIGHLIGHT_COLOR = "#ff6347"
const val PLAYER2_HIGHLIGHT_COLOR = "#7fffd4"
const val COLOR_SELECTED = "#ffefcc"


class CheckersGameViewModel(private val player1: Player<CheckersGame, CheckersMove>,
                            private val player2: Player<CheckersGame, CheckersMove>) {
    private val game = CheckersGame()
    private val gameController = GameController(player1, player2, game)

    fun startGame() {
        GlobalScope.launch(Dispatchers.Default) {
            gameController.startNewGame()
        }
    }


    fun boardClicked(coordinate: Pair<Int, Int>) {
        when (gameController.getTurn()) {
            player1.player -> if (player1 is HumanPlayer<*, *, *>) player1.onBoardClicked(coordinate)
            player2.player -> if (player2 is HumanPlayer<*, *, *>) player2.onBoardClicked(coordinate)
            else -> TODO()
        }
    }

    val board = MutableObservable<Board>()
    fun setBoard(board: CheckersBoard) {
        console.info("VM-setBoard------------------------------------------------df----------")
        val isHuman = true//todo
        val squares = board.mapPieces { piece, row, col ->
            Square(
                    imageUrl = when (piece) {
                        is RegularPiece -> if (piece.owner == BoardGame.Player.Player2) URL_REG_PLAYER1 else URL_REG_PLAYER2
                        is Queen -> if (piece.owner == BoardGame.Player.Player2) URL_QUEEN_PLAYER1 else URL_QUEEN_PLAYER2
                        else -> null
                    },
                    colorHtml = if ((row + col) % 2 == 0) BLACK_COLOR else WHITE_COLOR,
                    isClickable = true//todo
             )
        }

        this.board.value = Board(board.size, squares)
    }


    private fun isHumanPlayer(player: BoardGame.Player): Boolean {
        return when (player) {
            player1.player -> player1 is HumanPlayer<*, *, *>
            player2.player -> player2 is HumanPlayer<*, *, *>
            else -> false
        }
    }


    init {
        gameController.addListener(object : GameController.IGameControllerListener<CheckersBoard> {
            override fun onMoveDecided(move: Move, board: CheckersBoard) {

            }

            override fun onBoardChanged(board: CheckersBoard) {
                console.info("debug: onBoardChanged boardView")
                setBoard(board)
            }

            override fun onTurnStarted(turn: BoardGame.Player) {
                console.info("turn started : ${turn.name}")
            }

            override fun onTurnEnded(turn: BoardGame.Player) {
                console.info("turn ended : ${turn.name}")
            }

            override fun onScoreChanged(player1Score: Int, player2Score: Int) {
                console.info("score updated :player1=$player1Score, player2=$player2Score")
            }

            override fun onGameEnded(winner: BoardGame.Player?, score: Int) {
                console.info("game ended,${if (winner == null) "draw" else "winner is ${winner.name}"}")
            }

            override fun onTimeoutTimerChanged(timeoutMillis: Long) {
                console.info("timeout millis =$timeoutMillis")
            }

        })

    }
}

inline fun <R, T : model.Board<out Piece>> T.mapPieces(crossinline mapping: (Piece?, row: Int, col: Int) -> R): List<List<R>> {
    return List(size) { row ->
        List(size) { col ->
            mapping(this[row, col], row, col)
        }
    }
}
