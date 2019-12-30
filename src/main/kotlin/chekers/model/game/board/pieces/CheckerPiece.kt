package chekers.model.game.board.pieces

import boradGames.game.board.Board
import boradGames.game.board.pieces.Piece
import chekers.model.game.SingleMove
import boradGames.game.board.BoardGame

abstract class CheckerPiece( owner: BoardGame.Player): Piece(owner) {
    abstract fun getPossibleMoves(pos: Pair<Int, Int>, board: Board<CheckerPiece>): List<SingleMove>
}