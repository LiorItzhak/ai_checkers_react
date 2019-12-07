package model.game.Checkers.pieces

import model.Board
import model.Piece
import model.game.Checkers.SingleMove
import model.game.BoardGame

abstract class CheckerPiece(val owner: BoardGame.Player): Piece() {
    abstract fun getPossibleMoves(pos: Pair<Int, Int>, board: Board<CheckerPiece>): List<SingleMove>
}