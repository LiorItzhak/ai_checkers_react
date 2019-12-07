package ui

import kotlinx.css.*
import kotlinx.html.js.onClickFunction
import model.game.Checkers.CheckersGame
import react.*
import react.dom.div
import styled.css
import styled.styledButton
import ui.SquareView.SquareComponent.Companion.square
import kotlin.browser.window

const val URL_REG_PLAYER1 = "img/red-pawn.png"
const val URL_REG_PLAYER2 = "img/blue-pawn.png"
const val URL_QUEEN_PLAYER1 = "img/red-king.png"
const val URL_QUEEN_PLAYER2 = "img/blue-king.png"

const val WHITE_COLOR = "#f2f2f2"
const val BLACK_COLOR = "#262626"
const val PLAYER1_HIGHLIGHT_COLOR = "#ff6347"
const val PLAYER2_HIGHLIGHT_COLOR = "#7fffd4"
const val COLOR_SELECTED = "#ffefcc"

abstract class View{
    open fun notifyStateChanged() {}
}

data class BoardView(val size: Int):View() {
    private var squares: List<List<SquareView>> = List(CheckersGame.BOARD_SIZE) { row ->
        List(CheckersGame.BOARD_SIZE) { col ->
            SquareView(Pair(row, col), null, null, false)
        }
    }

    private val onBoardClickedListeners = mutableListOf<IOnBoardClickedListener>()
    fun addOnBoardClickedListener(listener: IOnBoardClickedListener) = onBoardClickedListeners.add(listener)
    fun removeOnBoardClickedListener(listener: IOnBoardClickedListener) = onBoardClickedListeners.remove(listener)

    private fun notifyOnBoardClickedListeners(coordinate: Pair<Int, Int>){
        onBoardClickedListeners.forEach { it.onBoardClicked(this, coordinate) }
    }

    private var onBoardChange: ((BoardView) -> Unit)? = null
    fun getSquare(row: Int, col: Int) = squares[row][col]

    fun setSquares(squares: List<List<SquareView>>) {
        if (squares != this.squares) {
            this.squares = squares
            notifyStateChanged()
        }
    }


    override fun notifyStateChanged() {
        onBoardChange?.let { it(this) }
    }

    interface IOnBoardClickedListener{
        fun onBoardClicked(view: View, coordinate: Pair<Int, Int>)
    }

    class BoardUi : RComponent<BoardUi.BoardUiProps, BoardUi.BoardUiState>() {
        override fun RBuilder.render() {
            if (state.currentBoard == null) state.currentBoard = props.board
            state.currentBoard?.let { board ->
                for (row in 0 until board.size) {
                    for (col in 0 until board.size) {
                        square {
                            boardSize = board.size
                            val sqVm = board.getSquare(row, col)
                            coordinate = sqVm.coordinate
                            colorHtml = sqVm.colorHtml
                            imageUrl = sqVm.imageUrl
                            onClick = { board.notifyOnBoardClickedListeners(it) }

                        }
                    }
                    div {}//new row
                }
            }

            props.board.onBoardChange = {
                setState {
                    currentBoard = it
                }
            }
        }

        interface BoardUiProps : RProps {
            var board: BoardView
        }

        interface BoardUiState : RState {
            var currentBoard: BoardView?
        }

        companion object {
            fun RBuilder.board(handler: BoardUiProps.() -> Unit): ReactElement {
                return child(BoardUi::class) {
                    this.attrs(handler)
                }
            }
        }
    }


}


data class SquareView(val coordinate: Pair<Int, Int>, var color: Color? = null, val image: Image? = null, var isClickable :Boolean= false):View() {
    val colorHtml: String
        get() = when (color) {
            Color.Player1Highlight -> PLAYER1_HIGHLIGHT_COLOR
            Color.Player2Highlight -> PLAYER2_HIGHLIGHT_COLOR
            Color.Selected -> COLOR_SELECTED
            null -> if ((coordinate.first + coordinate.second) % 2 == 0) BLACK_COLOR else WHITE_COLOR
        }

    val imageUrl: String?
        get() = when (image) {
            Image.Player1Reg -> URL_REG_PLAYER1
            Image.Player2Reg -> URL_REG_PLAYER2
            Image.Player1Queen -> URL_QUEEN_PLAYER1
            Image.Player2Queen -> URL_QUEEN_PLAYER2
            null -> null
        }

    enum class Color { Player1Highlight, Player2Highlight, Selected }
    enum class Image { Player1Reg, Player2Reg, Player1Queen, Player2Queen }


    class SquareComponent : RComponent<SquareComponent.SquareProps, SquareComponent.SquareState>() {
        override fun RBuilder.render() {
            styledButton {
                css {
                    val minWithHeight = (minOf(window.innerWidth, window.innerHeight).px / props.boardSize) * 0.9
                    height = minWithHeight
                    width = minWithHeight
                    padding = 5.px.toString()
                    border = "none"
                    background = "no-repeat #fff"
                    backgroundOrigin = BackgroundOrigin.contentBox
                    backgroundSize = "contain"
                    backgroundColor = Color(props.colorHtml)

                    props.imageUrl?.let {
                        background = "no-repeat #fff"//image not repeat
                        backgroundImage = Image("url(\"$it\")")
                    }

                    if (props.isClickable)
                        cursor = Cursor.pointer
                }
                attrs {
                    onClickFunction = { (props.onClick)(props.coordinate) }
                }
            }
        }

        interface SquareProps : RProps {
            var colorHtml: String
            var boardSize: Int
            var coordinate: Pair<Int, Int>
            var imageUrl: String?
            var isClickable:Boolean //row,col
            var onClick : (Pair<Int,Int>)->Unit
        }


        interface SquareState : RState

        companion object {
            fun RBuilder.square(handler: SquareProps.() -> Unit): ReactElement {
                return child(SquareComponent::class) {
                    this.attrs(handler)
                }
            }
        }
    }

}
