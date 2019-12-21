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
        val result =  getMoves(board, owner, pos, deltas)
                .toMutableList().apply {
                    addAll(getMoves(board, owner, pos, intArrayOf(-1, 1)
                            .map { dx -> -dy to dx })
                            .filter { it.ate })
        }
        return if (result.any { it.ate }) result.filter { it.ate } else result
    }
}