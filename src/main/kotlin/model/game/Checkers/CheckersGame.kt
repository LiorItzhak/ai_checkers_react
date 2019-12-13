package model.game.Checkers

import cartesianFor
import kotlinx.coroutines.*
import model.game.Checkers.pieces.King
import model.game.Checkers.pieces.RegularPiece
import model.game.BoardGame
import model.game.Checkers.pieces.Queen


class CheckersGame : BoardGame<CheckersMove, CheckersBoard>(CheckersBoard(BOARD_SIZE)) {

    override var board: CheckersBoard = super.board
        private set

    companion object {
        const val BOARD_SIZE = 8
    }


    override fun copy(): CheckersGame {
        return CheckersGame().also {
            it.board = board.copy()
        }
    }

    override fun initBoard() {
        //TODO clear board before initiating
        cartesianFor(BOARD_SIZE / 2 - 1, (BOARD_SIZE + 1) / 2) { line, i ->
            board[line, 2 * i + line % 2] = RegularPiece(Player.Player2)
            board[BOARD_SIZE - 1 - line, 2 * i + (BOARD_SIZE - 1 - line) % 2] = RegularPiece(Player.Player1)
        }
        //  listeners.forEach { it.onBoardChanged(board.copy()) }
    }

    override fun applyMove(move: CheckersMove) {
        fun doMove(move: SingleMove) {
            if (move.atePos != null)
                board.remove(move.atePos)
            board[move.end] = board.remove(move.start)!!
        }
        val finalPos: Pair<Int, Int>
        val owner: Player
        when (move) {
            is SingleMove -> {
                finalPos = move.end
                owner = board[move.start]?.owner!!
                doMove(move)
            }
            is MultiMove -> {
                finalPos = move.moves.last().end
                owner = board[move.moves.first().start]?.owner!!
                move.moves.forEach { doMove(it) }
            }
            else -> {TODO()}
        }
        if ((finalPos.first == 0 && owner == Player.Player1)
                || (finalPos.first == BOARD_SIZE - 1 && owner == Player.Player2))
            board[finalPos] = Queen(owner)
    }

    override fun getRandomMove(player: Player): CheckersMove = getAllPossibleMoves(player).random()

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

    override fun isGameEnded(playerTurn: Player): Boolean = getAllPossibleMoves(playerTurn).isEmpty()

    override fun getScore(player: Player): Int {
        var score1 = 0
        var score2 = 0
        cartesianFor(BOARD_SIZE, BOARD_SIZE) { pos ->
            board[pos]?.let {
                val tmp = when (it) {
                    is RegularPiece -> 1
                    is King -> 2
                    is Queen -> 10
                    else -> 0
                }
                if (it.owner == player) score1 += tmp else score2 += tmp
            }
        }
        return when {
            score1 == 0 -> -1_000
            score2 == 0 -> 1_000
            else -> score1 - score2
        }
    }

    override fun hashCode(): Int {
        return board.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        return board == (other as? CheckersGame)?.board
    }
}
