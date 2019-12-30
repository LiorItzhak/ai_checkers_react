package chekers.chekers.model.game.board.pieces

import boradGames.game.board.Board
import chekers.model.game.SingleMove
import chekers.model.game.board.pieces.CheckerPiece
import boradGames.game.board.BoardGame
import plus
import times

fun <T : CheckerPiece> getMoves(board: Board<T>, player: BoardGame.Player, pos: Pair<Int, Int>, deltas: List<Pair<Int, Int>>): List<SingleMove> {
    val moves = arrayListOf<SingleMove>()
    deltas.forEach { delta ->
        //check if square is on board
        if (pos + delta in board) {
            //if square is empty add it to moves
            if (board.emptyAt(pos + delta))
                moves.add(SingleMove(pos, pos + delta))
            //check if you can eat the occupied square
            else if (pos + 2 * delta in board
                    && board.emptyAt(pos + 2 * delta)
                    && board[pos + delta]!!.owner != player)
                moves.add(SingleMove(pos, pos + 2 * delta, pos + delta))
        }
    }
    return if (moves.any { it.ate }) moves.filter { it.ate } else moves
}