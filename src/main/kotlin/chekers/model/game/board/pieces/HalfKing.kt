package chekers.model.game.board.pieces

import boradGames.game.board.Board
import boradGames.game.board.BoardGame
import chekers.model.game.SingleMove
import chekers.chekers.model.game.board.pieces.getMoves

class HalfKing(owner: BoardGame.Player): RegularPiece(owner) {

    override fun getPossibleMoves(pos: Pair<Int, Int>, board: Board<CheckerPiece>): List<SingleMove> {
        val dy = if (owner == BoardGame.Player.Player2) 1 else -1
        val moves = super.getPossibleMoves(pos, board)
                .toMutableList()
                .apply {
                    addAll(getMoves(board, owner, pos, intArrayOf(-1, 1)
                            .map { dx -> -dy to dx })
                            .filter { it.ate })
                }
        return if (moves.any { it.ate }) moves.filter { it.ate } else moves
    }
}