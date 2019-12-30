package chekers.model.game.board.pieces

import boradGames.game.board.Board
import chekers.model.game.SingleMove
import boradGames.game.board.BoardGame
import chekers.chekers.model.game.board.pieces.getMoves

open class RegularPiece(owner: BoardGame.Player): CheckerPiece(owner) {
    override fun getPossibleMoves(pos: Pair<Int, Int>, board: Board<CheckerPiece>): List<SingleMove> {
        require(pos in board) { "piece must be on board" }

        val dy = if (owner == BoardGame.Player.Player2) 1 else -1
        val deltas = intArrayOf(-1, 1).map { dx -> dy to dx }
        return getMoves(board, owner, pos, deltas)
    }
}