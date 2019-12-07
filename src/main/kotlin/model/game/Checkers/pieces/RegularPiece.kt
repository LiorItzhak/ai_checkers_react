package model.game.Checkers.pieces

import model.Board
import model.game.Checkers.SingleMove
import model.game.BoardGame

class RegularPiece(owner: BoardGame.Player): CheckerPiece(owner) {
    override fun getPossibleMoves(pos: Pair<Int, Int>, board: Board<CheckerPiece>): List<SingleMove> {
        require(pos in board) { "piece must be on board" }

        val moves = arrayListOf<SingleMove>()
        val dy = if (owner == BoardGame.Player.Player2) 1 else -1
        val deltas = intArrayOf(-1, 1).map { dx -> dy to dx }
        return getMoves(board, owner, pos, deltas)
    }
}