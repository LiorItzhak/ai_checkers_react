package model.game.Checkers.pieces

import model.Board
import model.game.Checkers.SingleMove
import model.game.BoardGame
import plus
import times

class Queen(owner: BoardGame.Player): CheckerPiece(owner) {

    override fun getPossibleMoves(pos: Pair<Int, Int>, board: Board<CheckerPiece>): List<SingleMove> {
        val moves = arrayListOf<SingleMove>()
        intArrayOf(-1, 1).flatMap { dy -> intArrayOf(-1, 1).map { dx -> dy to dx } }
                .forEach { delta ->
                    var i = 1
                    while (pos + i * delta in board) {
                        if (board.emptyAt(pos + i * delta))
                            moves.add(SingleMove(pos, pos + i * delta))
                        else {
                            if (board[pos + i * delta]!!.owner != owner) {
                                var j = i + 1
                                while (pos + j * delta in board && board.emptyAt(pos + j * delta)) {
                                    moves.add(SingleMove(pos, pos + j * delta, pos + i * delta))
                                    j++
                                }
                                break
                            }
                            break
                        }
                        i++
                    }
                }
        return if (moves.any { it.ate }) moves.filter { it.ate } else moves
    }
}