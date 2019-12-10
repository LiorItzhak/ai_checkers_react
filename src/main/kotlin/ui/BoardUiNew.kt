package ui

import kotlinx.css.*
import kotlinx.html.js.onClickFunction
import react.RBuilder
import react.dom.div
import styled.css
import styled.styledButton
import kotlin.browser.window

data class Square(val colorHtml: String, val imageUrl: String? = null,val isClickable : Boolean = false )
data class Board(val size: Int, val squares: List<List<Square>>){
     operator fun get(row: Int, col: Int): Square? = get(row to col)

     operator fun get(pos: Pair<Int, Int>): Square? = squares[pos.first][pos.second]
}

fun RBuilder.boardUi(board: Board,onBoardClick : (( Pair<Int, Int>)->Unit)? = null) {
    val squareSize = minOf(window.innerWidth, window.innerHeight).px * 0.9 / board.size
    for (row in 0 until board.size) {
        for (col in 0 until board.size) {
            val square = board.squares[row][col]
            styledButton {
                css {
                    width = squareSize
                    height = squareSize
                    padding = 5.px.toString()
                    border = "none"
                    background = "no-repeat #fff"
                    backgroundOrigin = BackgroundOrigin.contentBox
                    backgroundSize = "contain"
                    backgroundColor = Color(square.colorHtml)

                    square.imageUrl?.let {
                        background = "no-repeat #fff"//image not repeat
                        backgroundImage = Image("url(\"$it\")")
                    }
                    if (square.isClickable)
                        cursor = Cursor.pointer
                }
                attrs {
                    if (square.isClickable)
                        onClickFunction = { onBoardClick?.invoke(row to col) }
                }
            }
        }
        div {}//new row
    }
}
