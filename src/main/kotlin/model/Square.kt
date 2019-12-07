package model

open class Square(val x: Int, val y: Int, open val piece: Piece? = null) {

    fun getPos() = Pair(x,y)

    fun isEmpty() = (piece==null)
}

class MutableSquare(x: Int, y: Int, override var piece: Piece? = null) : Square(x,y)