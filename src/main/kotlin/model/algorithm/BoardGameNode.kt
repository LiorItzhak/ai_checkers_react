package model.algorithm

import model.game.BoardGame
import model.game.Checkers.Move

class BoardGameNode<T: Move>(private val game: BoardGame<T, *>,
                             private val owner: BoardGame.Player,
                             private val currentPlayer: BoardGame.Player = owner): TreeNode<T> {
    companion object {
        private fun oppositePlayer(player: BoardGame.Player) =
                if(player==BoardGame.Player.Player1) BoardGame.Player.Player2 else BoardGame.Player.Player1
    }

    private suspend fun getChildForDelta(delta: T): TreeNode<T> {
        val newGame = game.copy().apply { applyMove(delta, 0) }
        return BoardGameNode(newGame, owner, oppositePlayer(currentPlayer))
    }

    override suspend fun getChildrenWithDeltas(): List<Pair<TreeNode<T>, T>> {
        return game.getAllPossibleMoves(currentPlayer)
                .map { move -> getChildForDelta(move) to move }
    }

    override fun getScore(): Int {
        return game.getScore(owner)
    }
}