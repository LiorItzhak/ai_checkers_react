package chekers.model.game

import cartesianFor
import chekers.model.game.board.CheckersBoard
import chekers.model.game.board.pieces.King
import chekers.model.game.board.pieces.RegularPiece
import boradGames.game.board.BoardGame
import chekers.model.game.board.pieces.RegularPieceEatBackward
import chekers.model.game.board.pieces.Queen


open class CheckersGame(protected val firstPlayer: Player = Player.Player1,boardSize:Int = BOARD_SIZE) : BoardGame<CheckersMove, CheckersBoard>(CheckersBoard(boardSize)) {

    var drawStepCounter = 0
    private set
    private var movedCached = false

    //TODO cache all possible moves (until apply move called)
    override var board: CheckersBoard = super.board
        protected set

    companion object {
        const val BOARD_SIZE = 8
    }


    override fun copy(): CheckersGame {
        return CheckersGame(firstPlayer).also {
            it.board = board.copy()
            it.currentPlayer = currentPlayer
            it.drawStepCounter = drawStepCounter
        }
    }

    override fun initBoard() {
        currentPlayer = firstPlayer
        board.clear()

        cartesianFor(board.size / 2 - 1, (board.size + 1) / 2) { line, i ->
            board[line, 2 * i + (line + 1) % 2] = RegularPiece(Player.Player2)
            board[board.size - 1 - line, 2 * i + (board.size - line) % 2] = RegularPiece(Player.Player1)
        }
    }

    override fun applyMove(move: CheckersMove) {
        fun doMove(move: SingleMove) {
            val piece = board.remove(move.start)
                    ?: throw IllegalArgumentException("Move is illegal: no piece at ${move.start}")

            require(piece.owner == currentPlayer) { "piece owner was ${piece.owner}. but only $currentPlayer can make a turn at this time" }
            board[move.end] = piece

            if (move.atePos != null)
                board.remove(move.atePos)
        }

        val finalPos: Pair<Int, Int>
        when (move) {
            is SingleMove -> {
                //update queen counter to catch draws
                if (board[move.start] !is RegularPiece && !move.ate) drawStepCounter++ else drawStepCounter = 0
                finalPos = move.end
                doMove(move)
            }
            is MultiMove -> {
                //defiantly ate piece in a multiMove: restore draw counter
                drawStepCounter = 0
                finalPos = move.moves.last().end
                move.moves.forEach { doMove(it) }
            }
            else -> throw UnsupportedOperationException("Unknown move type: can't apply move")
        }
        if ((finalPos.first == board.size - 1 && currentPlayer == Player.Player2)
                || (finalPos.first == 0 && currentPlayer == Player.Player1))
            board[finalPos] = Queen(currentPlayer)

        //remove cache, change current player
        movedCached = false
        currentPlayer = currentPlayer.getOpponent()
    }

    override fun getRandomMove(player: Player): CheckersMove = possibleMoves().random()

    private fun getAllPossibleMoves(player: Player): List<CheckersMove> {
        val moves = mutableListOf<SingleMove>()
        cartesianFor(board.size, board.size) { row, col ->
            board[row, col]?.let { p ->
                if (p.owner == player)
                    moves += p.getPossibleMoves(row to col, board)
            }
        }
        return if (moves.any { it.ate }) {
            val result = moves.filter { it.ate }
                    .flatMap { startMove ->
                        val eatenP = board.remove(startMove.atePos!!)// save eaten piece
                        val original = board.remove(startMove.start)!!
                        //change piece to king to enable eating backwards
                        board[startMove.end] = if (original is RegularPiece) RegularPieceEatBackward(original.owner) else original

                        val nextMoves = getAllPossibleMoves(player)
                                .filter { it.ate }
                                .filter {
                                    when (it) {
                                        is SingleMove -> (it.start == startMove.end)
                                        is MultiMove -> it.moves[0].start == startMove.end
                                        else -> false
                                    }
                                }
                        //restore board
                        board[startMove.atePos] = eatenP!!
                        board[startMove.start] = original
                        board.remove(startMove.end)!!

                        if (nextMoves.isNotEmpty())
                            nextMoves.map { MultiMove(startMove, it) }
                        else
                            listOf(startMove)
                    }
            result
        } else moves
    }

    override fun getScore(player: Player): Double {
        fun getDistFromEnd(pos: Pair<Int, Int>, owner: Player): Double {
            return if (owner==Player.Player2)
                pos.first.toDouble() / board.size
            else
                (board.size-1-pos.first).toDouble() / board.size
        }

        if (drawStepCounter>=15) return 0.0

        var score1 = 0.0 ;var score2 = 0.0
        cartesianFor(board.size, board.size) { pos ->
            board[pos]?.let {
                when (it) {
                    is RegularPiece -> if (it.owner==player) score1+= (1+getDistFromEnd(pos, it.owner)) else score2 += (1+getDistFromEnd(pos, it.owner))
                    is King -> if (it.owner==player) score1+=2 else score2+=2
                    is Queen -> if (it.owner==player) score1+=10 else score2+=10
                    else -> {}
                }
            }
        }
        return when {
            score1 == 0.0 -> -50.0
            score2 == 0.0 -> 50.0
            else -> score1 - score2
        }
    }

    override fun hashCode(): Int {
        return board.hashCode() +31*currentPlayer.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        return board == (other as? CheckersGame)?.board && currentPlayer == other.currentPlayer && drawStepCounter == other.drawStepCounter
    }

    private var possibleMoves: List<CheckersMove>? = null
    override fun possibleMoves(): List<CheckersMove> {
        if (possibleMoves!=null && movedCached)
            return possibleMoves as List<CheckersMove>

        possibleMoves = getAllPossibleMoves(currentPlayer)
        movedCached = true
        return possibleMoves as List<CheckersMove>
    }

    override fun isEnded(): Boolean {
        return (drawStepCounter >= 15) || possibleMoves().isEmpty()
    }

}
