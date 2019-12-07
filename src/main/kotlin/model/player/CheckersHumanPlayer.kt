package model.player

import model.game.Checkers.CheckersMove


import model.game.Checkers.CheckersGame
import model.game.Checkers.MultiMove
import model.game.Checkers.SingleMove

class CheckersHumanPlayer(name: String? = null) : HumanPlayer<CheckersGame, CheckersMove, HumanPlayer.HumanMove<CheckersMove>>(name ?: "human Player") {

    override suspend fun humanMove(game: CheckersGame, clickCoordinate: Pair<Int, Int>, previousPart: HumanMove<CheckersMove>?): HumanMove<CheckersMove> {
        console.info("debug: human clicked : $clickCoordinate ")
        when {
            previousPart == null || previousPart.data is Unit -> {
                //the human has clicked on the tool that he want to pick up
                return if (game.getAllPossibleMoves(player).any { it.isStartWith(clickCoordinate) }) {
                    console.info("debug: human pickup tool: $clickCoordinate ")
                    HumanMove(move = null, waitForAnotherClick = true, data = clickCoordinate)
                } else {
                    console.info("debug: human click on invalid coordinate: $clickCoordinate ")
                    resetMove()
                }
            }

            previousPart.data is Pair<*, *> -> {
                //the human has completed the first part of the move - create a single move
                @Suppress("UNCHECKED_CAST")
                val previousClick: Pair<Int, Int> = previousPart.data as Pair<Int, Int>
                val move = game.getAllPossibleMoves(player)
                        .firstOrNull {
                            when (val singleMove = if (it is MultiMove && it.moves.isNotEmpty()) it.moves[0] else it) {
                                is SingleMove -> singleMove.start == previousClick && singleMove.end == clickCoordinate
                                else -> TODO("human player dont know how to handle this move $singleMove")
                            }
                        }

                return when (move) {
                    null -> resetMove()//illegal move, reset to first pos
                    is SingleMove -> HumanMove(move = move)
                    is MultiMove -> HumanMove(move = move.moves[0], waitForAnotherClick = true)
                    else -> TODO("human player dont know how to handle this move $move")
                }
            }
            else -> {
                //user collected a multimove, expand the multimove
                val previousMove = previousPart.move ?: throw RuntimeException("bug:previous move cant be null")
                //get a valid move that start with the previous part and continue with the give coordinates
                val move = game.getAllPossibleMoves(player)
                        .filter { it is MultiMove && it.isStartWith(previousMove) }
                        .firstOrNull { it is MultiMove && it.isContinueWith(previousMove, clickCoordinate) } as? MultiMove

                if(move ==null) {
                    //illegal move, reset to first pos
                    console.info("human player :cant find a move that start with $previousMove and continue with $clickCoordinate")
                    console.info("start with options= ${ game.getAllPossibleMoves(player).filter { it is MultiMove && it.isStartWith(previousMove) }}")
                    console.info("continue with options= ${ game.getAllPossibleMoves(player).filter {it is MultiMove && it.isContinueWith(previousMove, clickCoordinate)}}")
                    return resetMove()
                }

                val relevantMoveSize = move.moves.size
                move.apply { removeAllAfter(if(previousMove is MultiMove) previousMove.moves.size  else 1) }
                console.info("human player :collected $move")
                return HumanMove(move = move , waitForAnotherClick = move.moves.size < relevantMoveSize)
            }
        }
    }









    private fun resetMove(): HumanMove<CheckersMove> {
        console.info("human player reset move")
        //mark as reset with Unit in the data
        return HumanMove(move = null, waitForAnotherClick = true, data = Unit)//illegal move, reset to first pos
    }

    private fun CheckersMove.isStartWith(firstCoordinate: Pair<Int, Int>): Boolean {
        return when (this) {
            is SingleMove -> start == firstCoordinate
            is MultiMove -> moves[0].start == firstCoordinate
            else -> TODO("human player dont know how to handle this move $this")
        }
    }

    private fun MultiMove.isStartWith(checkersMove: CheckersMove): Boolean {
        return when (checkersMove) {
            is SingleMove -> moves[0] == checkersMove
            is MultiMove -> moves.size >= checkersMove.moves.size && moves.slice(0..checkersMove.moves.lastIndex) == checkersMove.moves
            else -> TODO("human player dont know how to handle this move $checkersMove")
        }
    }

    private fun MultiMove.isContinueWith(checkersMove: CheckersMove, nextCoordinate: Pair<Int, Int>): Boolean {
        return when (checkersMove) {
            is SingleMove -> moves[1].end == nextCoordinate
            is MultiMove -> moves[checkersMove.moves.lastIndex+1].end == nextCoordinate
            else -> TODO("human player dont know how to handle this move $checkersMove")
        }
    }

    private fun MultiMove.removeAllAfter(index: Int) {
        val movesSlice: MutableList<SingleMove> = mutableListOf()
        moves.forEachIndexed { i, m ->
            if (i <= index)
                movesSlice.add(m)
        }
        moves.clear()
        moves.addAll(movesSlice)
    }


}