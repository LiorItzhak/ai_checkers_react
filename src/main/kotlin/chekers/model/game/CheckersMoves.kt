package chekers.model.game

import boradGames.game.Move


abstract class CheckersMove(val ate: Boolean) : Move()

data class SingleMove(val start: Pair<Int, Int>,
                      val end: Pair<Int, Int>,
                      val atePos: Pair<Int, Int>? = null) : CheckersMove(atePos != null) {

    override fun toString(): String = "$start->$end)${if (ate) " kill:$atePos" else ""}"
}

class MultiMove(start: SingleMove, endMoves: CheckersMove) : CheckersMove(true) {
    val moves: MutableList<SingleMove> = mutableListOf(start)

    init {
        if (endMoves is MultiMove)
            moves += endMoves.moves
        else if (endMoves is SingleMove)
            moves += endMoves
    }

    override fun toString(): String {
        val s =moves.slice(0..moves.lastIndex)
                .map { "${it.start}" }
                .reduce { s1, s2 -> "$s1->$s2" }
        return "$s->${moves.last().end} ,kill${moves.map { "${it.atePos}" }.reduce{m1,m2->"${m1},${m2}"}}"
    }


    override fun equals(other: Any?): Boolean = moves == (other as? MultiMove)?.moves
    override fun hashCode(): Int = moves.hashCode()

}