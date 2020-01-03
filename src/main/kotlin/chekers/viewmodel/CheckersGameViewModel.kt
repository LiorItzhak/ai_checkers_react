package chekers.viewmodel

import utills.MutableObservable
import chekers.model.game.*
import chekers.model.game.board.CheckersBoard
import kotlinx.coroutines.*
import boradGames.game.board.pieces.Piece
import boradGames.game.board.BoardGame
import chekers.model.game.board.pieces.Queen
import chekers.model.game.board.pieces.RegularPiece
import boradGames.game.GameController
import boradGames.game.Move
import chekers.model.players.CheckersHumanPlayer
import boradGames.players.HumanPlayer
import boradGames.players.Player
import chekers.ui.Board
import chekers.ui.Square
import kotlin.browser.window

const val URL_REG_PLAYER1 = "img/red-pawn.png"
const val URL_REG_PLAYER2 = "img/blue-pawn.png"
const val URL_QUEEN_PLAYER1 = "img/red-king.png"
const val URL_QUEEN_PLAYER2 = "img/blue-king.png"

const val WHITE_COLOR = "#f2f2f2"
const val BLACK_COLOR = "#262626"
const val PLAYER1_HIGHLIGHT_COLOR = "#ff6347"
const val PLAYER2_HIGHLIGHT_COLOR = "#7fffd4"
const val COLOR_SELECTED = "#ffefcc"


class CheckersGameViewModel(
        game: CheckersGame,
        private var player1: Player<CheckersGame, CheckersMove>,
        private var player2: Player<CheckersGame, CheckersMove>,
        timeLimitMillisPlayer1: Long?,
        timeLimitMillisPlayer2: Long? = timeLimitMillisPlayer1)
    : GameController.IGameControllerListener<CheckersGame, CheckersBoard>, HumanPlayer.IHumanPlayerListener<CheckersGame, CheckersMove> {
    val board = MutableObservable<Board>()//the observer view is notified when the value changes
    val timerSec = MutableObservable<Long?>()//the observer view is notified when the value changes
    private var gameController = GameController(player1, player2, game, timeLimitMillisPlayer1, timeLimitMillisPlayer2).apply { addListener(this@CheckersGameViewModel) }


    fun startGame() {
        MainScope().launch(Dispatchers.Default) {
            gameController.startNewGame()
        }
    }


    fun resetGame(game: CheckersGame, player1: Player<CheckersGame, CheckersMove>,
                  player2: Player<CheckersGame, CheckersMove>, timeLimitMillisPlayer1: Long?,
                  timeLimitMillisPlayer2: Long? = timeLimitMillisPlayer1) {

        gameController.clear()
        (player1 as? CheckersHumanPlayer)?.apply { console.log("player 1 is human")  }?.addListener(this)
        (player2 as? CheckersHumanPlayer)?.addListener(this)
        this.player1 = player1;this.player2 =player2
        gameController = GameController(player1, player2, game, timeLimitMillisPlayer1, timeLimitMillisPlayer2)
                .apply { addListener(this@CheckersGameViewModel) }    }

    fun boardClicked(coordinate: Pair<Int, Int>) {
        when (gameController.getTurn()) {
            player1.player -> (player1 as? CheckersHumanPlayer)?.onBoardClicked(coordinate)
            player2.player -> (player2 as? CheckersHumanPlayer)?.onBoardClicked(coordinate)
            else -> TODO()
        }
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
        //register to gameController events, change the checkers.ui properly
        (player1 as? CheckersHumanPlayer)?.addListener(this)
        (player2 as? CheckersHumanPlayer)?.addListener(this)

    }


    override fun onMoveDecided(move: Move, board: CheckersBoard) {
        console.info("debug: onMoveDecided")

        val boardT = this@CheckersGameViewModel.board.value
        val moveSquares = when (move) {
            is SingleMove -> listOf(move.end)
            is MultiMove -> move.moves.filterIndexed { inx, _ -> inx > 0 }.map { it.end }
            else ->  throw IllegalArgumentException("dont know how to handle this move $move")
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


    override fun onTurnStarted(game: CheckersGame, turn: BoardGame.Player) {
        console.info("turn started : ${turn.name}")
        val board = board.value
        console.info("turn started mark moves: ${turn.name}")
        //enable click on possible moves
        val allClickableSquares = game.possibleMoves().flatMap { move ->
            when (move) {
                is SingleMove -> listOf(move.end, move.start)
                is MultiMove -> move.moves.map { it.end }.toMutableList().apply { add(move.moves[0].start) }
                else -> throw IllegalArgumentException("dont know how to handle this move $move")
            }
        }
        this@CheckersGameViewModel.board.value = board
                .mapIndexed { it, row, col -> it.copy(isClickable = allClickableSquares.any { it == row to col }) }
    }

    override fun onTurnEnded(game: CheckersGame, turn: BoardGame.Player) {
        console.info("turn ended : ${turn.name}")
        //disable board clicks
        val board = board.value.map { it.copy(isClickable = false) }
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
        this@CheckersGameViewModel.board.value = board.toBoardUi()
    }

    override suspend fun playMoveAnimation(board: CheckersBoard, move: Move) {
        console.info("play animation $move")
        if (move is MultiMove && board[move.moves[0].start]?.owner?.isHuman() == false) {
            move.moves.forEachIndexed { i, m ->
                console.info("play animation  part $i - $m")
                delay(500)
                console.info("delayed play animation  part $i - $m")
                //apply move
                val piece = board.remove(m.start)
                        ?: throw IllegalArgumentException("Move is illegal: no piece at ${m.start}")
                board[m.end] = piece
                if (m.atePos != null)
                    board.remove(m.atePos)

                console.info("applied play animation  part $i - $m")
                this@CheckersGameViewModel.board.value = board.toBoardUi()
            }
        }
    }

    private var timerId: Int? = null
    override fun onTimeoutTimerStart(timeoutMillis: Long) {
        console.info("start timeout millis =$timeoutMillis")
        timerId?.let { timerId = null;window.clearInterval(it) }
        timerSec.value = timeoutMillis / 1000
        timerId = window.setInterval(handler = {
            timerSec.value = (timerSec.value ?: 0) - 1
            if (timerSec.value ?: 0 <= 0) timerId?.let { timerId = null;window.clearInterval(it);timerSec.value = 0 }
        }, timeout = 1000)
    }

    override fun onTimeoutTimerEnd(timeoutMillis: Long) {
        console.info("end timeout millis =$timeoutMillis")
        timerSec.value = null
        timerId?.let { timerId = null;window.clearInterval(it) }
    }

    override fun onHumanTakeMove(player: BoardGame.Player, game: CheckersGame, move: HumanPlayer.HumanMove<CheckersMove>) {
        if (move.move == null && move.data == Unit) {
            //reset turn - draw original board and make the original squares clickable
            board.value = game.board.toBoardUi().mapIndexed { s, row, col ->
                val stored = board.value[row, col]!!
                if (stored.isClickable) s.copy(isClickable = true) else s
            }
        } else {
            //get relevant positions (for highlight)
            var allHighlightSquares = game.possibleMoves().filter {
                it is MultiMove && when (move.move) {
                    is SingleMove -> it.moves[0] == move.move
                    is MultiMove -> it.moves.size >= move.move.moves.size && it.moves.slice(0..move.move.moves.lastIndex) == move.move.moves
                    null -> true
                    else -> TODO()
                } || (it is SingleMove && move.move == null) //single move means that the user a pick up new tool
            }.flatMap { m ->
                when (m) {
                    is SingleMove -> listOf(m.start to m.end)
                    is MultiMove -> m.moves.map { m.moves[0].start to it.end }
                    else -> TODO()
                }
            }

            val b: Board
            if (move.move == null && move.data is Pair<*, *>) {
                //human have pick up a tool
                //mark coordinates as selected
                @Suppress("UNCHECKED_CAST")
                val coordinates = move.data as Pair<Int, Int>
                b = board.value.mapIndexed { s, row, col ->
                    if (row to col == coordinates) {
                        s.copy(colorHtml = COLOR_SELECTED)
                    } else s
                }
                //highlight available moves- moves that start with the selected coordinates
                allHighlightSquares = allHighlightSquares.filter { it.first == coordinates }//take only moves that uses the picked tool
            } else if (move.move != null) {
                //human applied move
                //draw received moves - keep clickable positions
                b = game.copy().apply { applyMove(move = move.move) }.board.toBoardUi().mapIndexed { s, row, col ->
                    val stored = board.value[row, col]!!
                    if (stored.isClickable) s.copy(isClickable = true) else s
                }

                //take only moves that start with the given moves, than filter parts that already played
                allHighlightSquares = allHighlightSquares.filter {
                    when (val moveT = move.move) {
                        is SingleMove -> it.first == moveT.start && it.second != moveT.end
                        is MultiMove -> it.first == moveT.moves[0].start && !moveT.moves.map { m -> m.end }.contains(it.second)
                        else -> TODO()
                    }
                }
            } else throw RuntimeException("Bug - not suppose to get here 156478")

            board.value = b.mapIndexed { s, row, col ->
                if (allHighlightSquares.any { it.second == row to col }) {
                    when (player) {
                        BoardGame.Player.Player1 -> s.copy(colorHtml = PLAYER1_HIGHLIGHT_COLOR)
                        BoardGame.Player.Player2 -> s.copy(colorHtml = PLAYER2_HIGHLIGHT_COLOR)
                    }
                } else s
            }
        }
    }
}


inline fun <R, T : boradGames.game.board.Board<out Piece>> T.mapPieces(crossinline mapping: (Piece?, row: Int, col: Int) -> R): List<List<R>> {
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


fun CheckersBoard.toBoardUi(): Board {
    val squares = this.mapPieces { piece, row, col ->
        Square(
                imageUrl = when (piece) {
                    is RegularPiece -> if (piece.owner == BoardGame.Player.Player1) URL_REG_PLAYER1 else URL_REG_PLAYER2
                    is Queen -> if (piece.owner == BoardGame.Player.Player1) URL_QUEEN_PLAYER1 else URL_QUEEN_PLAYER2
                    else -> null
                },
                colorHtml = if ((row + col) % 2 == 1) BLACK_COLOR else WHITE_COLOR
        )
    }
    return Board(this.size, squares)
}


