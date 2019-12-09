//package ui
//
//import react.*
//import react.dom.h1
//import ui.BoardView.BoardUi.Companion.board
//import viewmodel.GameViewModel
//
//class GameUi : RComponent<GameUi.GameUiProps, GameUi.GameUiState>()
//{
//    override fun RBuilder.render() {
//        h1 { +"Hello World!3" }
//        state.currentBoard?.let {
//            board { board = it }
//        }
//
//        props.gameViewModel.board.obs
//    }
//
//    interface GameUiProps : RProps {
//       var gameViewModel : GameViewModel
//    }
//
//    interface GameUiState : RState {
//        var currentBoard: BoardView?
//
//    }
//
//    companion object {
//        fun RBuilder.game(handler: GameUiProps.() -> Unit): ReactElement {
//            return child(GameUi::class) {
//                this.attrs(handler)
//            }
//        }
//    }
//}