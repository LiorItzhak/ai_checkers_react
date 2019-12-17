package model.game.Checkers

import cartesianFor
import model.game.Checkers.pieces.King
import model.game.Checkers.pieces.RegularPiece
import model.game.BoardGame
import model.game.Checkers.pieces.Queen


class CheckersGame(private val firstPlayer: Player = Player.Player1) : BoardGame<CheckersMove, CheckersBoard>(CheckersBoard(BOARD_SIZE)) {

    private var drawStepCounter = 0
    private var movedCached = false

    //TODO cache all possible moves (until apply move called)
    override var board: CheckersBoard = super.board
        private set

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

        cartesianFor(BOARD_SIZE / 2 - 1, (BOARD_SIZE + 1) / 2) { line, i ->
            board[line, 2 * i + line % 2] = RegularPiece(Player.Player2)
            board[BOARD_SIZE - 1 - line, 2 * i + (BOARD_SIZE - 1 - line) % 2] = RegularPiece(Player.Player1)
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
                if (board[move.start] is Queen && !move.ate) drawStepCounter++ else drawStepCounter = 0
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
        if ((finalPos.first == BOARD_SIZE - 1 && currentPlayer == Player.Player2)
                || (finalPos.first == 0 && currentPlayer == Player.Player1))
            board[finalPos] = Queen(currentPlayer)

        //remove cache, change current player
        movedCached = false
        currentPlayer = currentPlayer.getOpponent()
    }

    override fun getRandomMove(player: Player): CheckersMove = possibleMoves().random()

    override fun getAllPossibleMoves(player: Player): List<CheckersMove> {
        val moves = mutableListOf<SingleMove>()
        cartesianFor(BOARD_SIZE, BOARD_SIZE) { row, col ->
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
                        board[startMove.end] = if (original is RegularPiece) King(original.owner) else original

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

    override fun isGameEnded(playerTurn: Player): Boolean = (drawStepCounter >= 15) || possibleMoves().isEmpty()

    override fun getScore(player: Player): Int {
        if (drawStepCounter>=15) return 0
        if (possibleMoves().isEmpty()) return -50

        var score1 = 0
        var score2 = 0
        cartesianFor(BOARD_SIZE, BOARD_SIZE) { pos ->
            board[pos]?.let {
                when (it) {
                    is RegularPiece -> if (it.owner==player) score1+=1 else score2+=1
                    is King -> if (it.owner==player) score1+=2 else score2+=2
                    is Queen -> if (it.owner==player) score1+=10 else score2+=10
                    else -> {}
                }
            }
        }
        return when {
            score1 == 0 -> -50
            score2 == 0 -> 50
            else -> score1 - score2
        }
    }

    override fun hashCode(): Int {
        return board.hashCode() +31*currentPlayer.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        return board == (other as? CheckersGame)?.board && currentPlayer == other.currentPlayer
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
        return isGameEnded(currentPlayer)
    }

}
