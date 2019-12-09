package viewmodel

import Utills.MutableObservable
import kotlinx.coroutines.*
import model.Piece
import model.game.BoardGame
import model.game.Checkers.*
import model.game.Checkers.pieces.Queen
import model.game.Checkers.pieces.RegularPiece
import model.game.GameController
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
    val board = MutableObservable<Board>()//the observer view is notified when the value changes
    val timer = MutableObservable<Long>()//the observer view is notified when the value changes

    private val game = CheckersGame()
    private val gameController = GameController(player1, player2, game)


    fun startGame() {
        //start game on different coroutine
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

    //get a CheckersBoard and map it for the view
    fun setBoard(board: CheckersBoard) {
        val squares = board.mapPieces { piece, row, col ->
            Square(
                    imageUrl = when (piece) {
                        is RegularPiece -> if (piece.owner == BoardGame.Player.Player1) URL_REG_PLAYER1 else URL_REG_PLAYER2
                        is Queen -> if (piece.owner == BoardGame.Player.Player1) URL_QUEEN_PLAYER1 else URL_QUEEN_PLAYER2
                        else -> null
                    },
                    colorHtml = if ((row + col) % 2 == 0) WHITE_COLOR  else BLACK_COLOR
            )
        }

        this.board.value = Board(board.size, squares)
    }

    //todo - for human clicks (clickable squares)
    private fun BoardGame.Player.isHuman(): Boolean {
        return when (this) {
            player1.player -> player1 is HumanPlayer<*, *, *>
            player2.player -> player2 is HumanPlayer<*, *, *>
            else -> false
        }
    }


    init {
        //register to gameController events, change the ui properly
        gameController.addListener(object : GameController.IGameControllerListener<CheckersGame, CheckersBoard> {
            override fun onMoveDecided(move: Move, board: CheckersBoard) {
                console.info("debug: onMoveDecided")

                val boardT = this@CheckersGameViewModel.board.value ?: return
                val moveSquares = when (move) {
                    is SingleMove -> listOf(move.end)
                    is MultiMove -> move.moves.filterIndexed { inx, _ -> inx > 0 }.map { it.end }
                    else -> TODO()
                }

                val b = boardT.mapIndexed { s, row, col ->
                    if (moveSquares.any { it == row to col }) {
                        val color = when (gameController.getTurn()) {
                            BoardGame.Player.Player1 -> PLAYER1_HIGHLIGHT_COLOR
                            BoardGame.Player.Player2 -> PLAYER2_HIGHLIGHT_COLOR
                        }
                        s.copy(colorHtml = color)
                    } else s
                }
                this@CheckersGameViewModel.board.value = b
            }


            override fun onTurnStarted(turn: BoardGame.Player) {
                console.info("turn started : ${turn.name}")
                val board = board.value ?: return
                console.info("turn started mark moves: ${turn.name}")
                //enable click on posible moves
                val allClickableSquares = game.getAllPossibleMoves(turn).flatMap { move ->
                    when (move) {
                        is SingleMove -> listOf(move.end, move.start)
                        is MultiMove -> move.moves.map { it.end }.toMutableList().apply { add(move.moves[0].start) }
                        else -> TODO()
                    }
                }
                this@CheckersGameViewModel.board.value = board
                        .mapIndexed { it, row, col -> it.copy(isClickable = allClickableSquares.any { it == row to col }) }
            }

            override fun onTurnEnded(turn: BoardGame.Player) {
                console.info("turn ended : ${turn.name}")
                //disable board clicks
                val board = board.value?.map { it.copy(isClickable = false) } ?: return
                this@CheckersGameViewModel.board.value = board
            }

            override fun onScoreChanged(player1Score: Int, player2Score: Int) {
                console.info("score updated :player1=$player1Score, player2=$player2Score")
            }

            override fun onGameEnded(winner: BoardGame.Player?, score: Int) {
                console.info("game ended,${if (winner == null) "draw" else "winner is ${winner.name}"}")
            }



            override fun onBoardChanged(board: CheckersBoard) {
                console.info("debug: onBoardChanged")
                setBoard(board)
            }


            override suspend fun playMoveAnimation(game: CheckersGame, move: Move) {
                console.info("play animation $move")
                if (move is MultiMove && game.board[move.moves[0].start]?.owner?.isHuman() == false) {
                    move.moves.forEachIndexed { i, m ->
                        console.info("play animation  part $i - $m")
                        delay(500)
                        console.info("delayed play animation  part $i - $m")
                        game.applyMove(m)
                        console.info("applied play animation  part $i - $m")
                        setBoard(game.board)
                    }

                }
            }

            override fun onTimeoutTimerStart(timeoutMillis: Long) {
                console.info("start timeout millis =$timeoutMillis")
            }

            override fun onTimeoutTimerEnd(timeoutMillis: Long) {
                console.info("end timeout millis =$timeoutMillis")
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

inline fun Board.mapIndexed(crossinline mapping: (Square, row: Int, col: Int) -> Square): Board {
    val s = List(size) { row ->
        List(size) { col ->
            mapping(this[row, col]!!, row, col)
        }
    }
    return Board(size, s)
}

inline fun Board.map(crossinline mapping: (Square) -> Square): Board {
    val s = List(size) { row ->
        List(size) { col ->
            mapping(this[row, col]!!)
        }
    }
    return Board(size, s)
}

//val allHighlightSquares = possibleMoves.flatMap { move ->
//    when (move) {
//        is SingleMove -> listOf(move.end)
//        is MultiMove -> move.moves.filterIndexed { inx, _ -> inx > 0 }.map { it.end }
//        else -> TODO()
//    }
//}
//val allSelectedSquares = possibleMoves.map { move ->
//    when (move) {
//        is SingleMove -> move.start
//        is MultiMove -> move.moves[0].start
//        else -> TODO()
//    }
//}
//
