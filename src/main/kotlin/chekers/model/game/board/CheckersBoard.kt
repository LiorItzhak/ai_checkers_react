package chekers.model.game.board

import boradGames.game.board.MutableBoard
import chekers.model.game.board.pieces.CheckerPiece

class CheckersBoard(size: Int): MutableBoard<CheckerPiece>(size) {

    override fun copy(): CheckersBoard {
        return CheckersBoard(size).also{
            //TODO copy pieces too
            it.pieces.putAll(pieces)
        }
    }
}