package boradGames.players


import boradGames.game.board.Board
import boradGames.game.board.pieces.Piece

import boradGames.game.board.BoardGame
import boradGames.game.Move


abstract class AiPlayer<G : BoardGame<M, out Board<out Piece>>, M : Move>(name: String) : Player<G, M>(name)







