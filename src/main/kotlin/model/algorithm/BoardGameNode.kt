package model.algorithm

import model.game.BoardGame
import model.game.Checkers.Move

class BoardGameNode<T: Move>(private val game: BoardGame<T, *>,
                                  private val owner: BoardGame.Player): TreeNode<T> {


    private fun getChildForDelta(delta: T): TreeNode<T> {
        val newGame = game.copy().apply { applyMove(delta) }
        return BoardGameNode(newGame, owner)
    }

    override suspend fun getChildrenWithDeltas(): List<Pair<TreeNode<T>, T>> {
        return game.possibleMoves()
                .map { move -> getChildForDelta(move) to move }
    }

    override fun getScore(): Int {
        return game.getScore(owner)
    }

    override val isTerminal: Boolean
        get() = game.possibleMoves().isEmpty() || game.isEnded()

    override fun hashCode(): Int {
        return game.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        return (other as? BoardGameNode<*>)?.game == game
    }
}