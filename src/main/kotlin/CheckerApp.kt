import Utills.observeForever
import react.*
import ui.Board
import ui.boardUi
import viewmodel.CheckersGameViewModel

interface AppState : RState {
    var board: Board?
}

interface CheckerAppProps : RProps {
    var viewModel: CheckersGameViewModel

}

class CheckerApp : RComponent<CheckerAppProps, AppState>() {
    override fun RBuilder.render() {
        bindToViewModel()
        val viewmodel =props.viewModel
        if (state.board != null)
            boardUi(state.board!!, onBoardClick = {viewmodel.boardClicked(it) })

    }

    private var isBinded = false
    private fun bindToViewModel(){
        if(!isBinded){
            isBinded = true
            props.viewModel.board.observeForever {
                console.info("setState-----------------------------------------------df----------")
                setState { board = it}
            }
        }

    }
}

fun RBuilder.checkerApp(handler: CheckerAppProps.() -> Unit): ReactElement {
    return child(CheckerApp::class) {
        this.attrs(handler)
    }
}


