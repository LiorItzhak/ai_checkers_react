package model

abstract class Board<T : Piece>(val size: Int) {

    protected val pieces = HashMap<Pair<Int, Int>, T>()

    open operator fun get(row: Int, col: Int): T? = get(row to col)

    open operator fun get(pos: Pair<Int, Int>): T? = pieces[pos]

    fun emptyAt(pos: Pair<Int, Int>): Boolean = pieces[pos] == null

    fun emptyAt(row: Int, col: Int) = emptyAt(row to col)

    operator fun contains(pos: Pair<Int, Int>): Boolean = (pos.first in 0 until size) && (pos.second in 0 until size)

    override fun equals(other: Any?): Boolean {
        return (other as? Board<T>)?.pieces?.equals(pieces) ?: false
    }

    abstract fun copy(): Board<T>

    override fun hashCode(): Int {
        return pieces.hashCode()
    }
}


abstract class MutableBoard<T : Piece>(size: Int) : Board<T>(size) {
    operator fun set(pos: Pair<Int, Int>, piece: T) {
        pieces[pos] = piece
    }

    operator fun set(row: Int, col: Int, piece: T) {
        set(row to col, piece)
    }

    fun remove(pos: Pair<Int, Int>): T? = pieces.remove(pos)

    fun clear() {
        pieces.clear()
    }
}