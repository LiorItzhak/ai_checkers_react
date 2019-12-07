package model.game.Checkers.pieces

import model.Board
import model.game.Checkers.SingleMove
import model.game.BoardGame

class King(player: BoardGame.Player): CheckerPiece(player) {
    override fun getPossibleMoves(pos: Pair<Int, Int>, board: Board<CheckerPiece>): List<SingleMove> {
        require(pos in board) { "piece must be on board" }

        val deltas = intArrayOf(-1, 1).flatMap { dy ->
            intArrayOf(-1, 1)
                    .map { dx -> dy to dx }
        }
        return getMoves(board, owner, pos, deltas)
    }
}