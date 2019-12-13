package model.algorithm

import model.game.BoardGame
import model.game.Checkers.Move

data class BoardGameNode<T: Move>(private val game: BoardGame<T, *>,
                                  private val owner: BoardGame.Player,
                                  private val currentPlayer: BoardGame.Player = owner): TreeNode<T> {

    private val children by lazy {
        game.getAllPossibleMoves(currentPlayer)
                .map { move -> getChildForDelta(move) to move } }

    private val score by lazy{ game.getScore(owner) }

    companion object {
        private fun oppositePlayer(player: BoardGame.Player) =
                if(player==BoardGame.Player.Player1) BoardGame.Player.Player2 else BoardGame.Player.Player1
    }

    private fun getChildForDelta(delta: T): TreeNode<T> {
        val newGame = game.copy().apply { applyMove(delta) }
        return BoardGameNode(newGame, owner, oppositePlayer(currentPlayer))
    }

    override suspend fun getChildrenWithDeltas(): List<Pair<TreeNode<T>, T>> {
        return children
    }

    override fun getScore(): Int {
        return score
    }
}