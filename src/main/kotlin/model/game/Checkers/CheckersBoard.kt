package model.game.Checkers

import model.MutableBoard
import model.game.Checkers.pieces.CheckerPiece

class CheckersBoard(size: Int): MutableBoard<CheckerPiece>(size) {

    override fun copy(): CheckersBoard{
        return CheckersBoard(size).also{
            //TODO copy pieces too
            it.pieces.putAll(pieces)
        }
    }
}