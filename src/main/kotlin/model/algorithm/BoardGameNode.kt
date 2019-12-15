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


    private fun getChildForDelta(delta: T): TreeNode<T> {
        val newGame = game.copy().apply { applyMove(delta) }
        return BoardGameNode(newGame, owner, currentPlayer.getOpponent())
    }

    override suspend fun getChildrenWithDeltas(): List<Pair<TreeNode<T>, T>> {
        return children
    }

    override fun getScore(): Int {
        return score
    }

    override val isTerminal: Boolean
        get() = children.isEmpty() || game.isGameEnded(currentPlayer)
}