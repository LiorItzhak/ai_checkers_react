package chekers.model.game.board.pieces

import boradGames.game.board.Board
import chekers.model.game.SingleMove
import boradGames.game.board.BoardGame
import chekers.chekers.model.game.board.pieces.getMoves

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