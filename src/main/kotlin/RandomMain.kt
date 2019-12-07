import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import model.algorithm.AlphaBetaAlgo
import model.algorithm.NegaMaxAlgo
import model.game.Checkers.*
import model.game.Checkers.CheckersGame.Companion.BOARD_SIZE
import model.game.Checkers.pieces.RegularPiece
import model.game.GameController
import model.game.BoardGame
import model.game.Checkers.pieces.Queen
import model.player.CheckersHumanPlayer
import model.player.CheckersAiPlayer
import react.dom.*
import ui.BoardView
import ui.BoardView.BoardUi.Companion.board
import ui.SquareView
import kotlin.browser.document
import kotlin.browser.window

fun main() {
    val game = CheckersGame()
    val player1 = CheckersAiPlayer(AlphaBetaAlgo(10))
    val player2 = CheckersAiPlayer(AlphaBetaAlgo(10))
    val gameController = GameController(player1, player2, game)

    val boardView = BoardView(BOARD_SIZE).apply {
//        addOnBoardClickedListener(player1)
    }

    GlobalScope.launch(Dispatchers.Default) {
        gameController.startNewGame()
    }

    window.onload = {
        render(document.getElementById("root")!!) {
            h1 { +"Hello World!2" }
            board { board = boardView }
        }
    }





    gameController.addListener(object : GameController.IGameControllerListener<CheckersBoard> {
        override fun onMoveDecided(move: Move, board: CheckersBoard) {
            when (move) {
                is SingleMove -> {
                    boardView.getSquare(move.start.first, move.start.second).color = SquareView.Color.Selected
                    boardView.getSquare(move.end.first, move.end.second).color =
                            if (board[move.start]!!.owner == BoardGame.Player.Player2)
                                SquareView.Color.Player1Highlight
                            else SquareView.Color.Player2Highlight
                }
                is MultiMove -> {
                    boardView.getSquare(move.moves[0].start.first, move.moves[0].start.second).color = SquareView.Color.Selected
                    move.moves.forEach { m ->
                        boardView.getSquare(m.end.first, m.end.second).color =
                                if (board[move.moves[0].start]!!.owner == BoardGame.Player.Player2)
                                    SquareView.Color.Player1Highlight
                                else SquareView.Color.Player2Highlight
                    }
                }
            }
            boardView.notifyStateChanged()
        }

        val imageList = mutableListOf<SquareView.Image?>(null).apply { addAll(SquareView.Image.values()) }

        override fun onBoardChanged(board: CheckersBoard) {
            console.info("debug: onBoardChanged boardView")
            boardView.setSquares(List(BOARD_SIZE) { row ->
                List(BOARD_SIZE) { col ->
                    val image = when(board[row, col]) {
                        is RegularPiece -> if (board[row, col]!!.owner == BoardGame.Player.Player2) imageList[1] else imageList[2]
                        is Queen -> if (board[row, col]!!.owner == BoardGame.Player.Player2) imageList[3] else imageList[4]
                        else -> null
                    }
                    SquareView(Pair(row, col), null, image, board[row, col] != null)
                }
            })
            boardView.notifyStateChanged()
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
            console.info("game ended,${if(winner ==null) "draw" else "winner is ${winner.name}"}")
        }

        override fun onTimeoutTimerChanged(timeoutMillis: Long) {
            console.info("timeout millis =$timeoutMillis")
        }

    })


}


