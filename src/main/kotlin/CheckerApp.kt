import Utills.observeForever
import kotlinx.coroutines.selects.whileSelect
import kotlinx.css.*
import kotlinx.html.DIV
import kotlinx.html.SELECT
import kotlinx.html.js.onChangeFunction
import kotlinx.html.onClick
import org.w3c.dom.HTMLSelectElement
import org.w3c.dom.events.Event
import react.*
import react.dom.*
import styled.*
import ui.Board
import ui.boardUi
import viewmodel.CheckersGameViewModel
import kotlin.browser.document
import kotlin.browser.window

interface AppState : RState {
    var board: Board?
    var timerSec: Long?

}

interface CheckerAppProps : RProps {
    var viewModel: CheckersGameViewModel
}

class CheckerApp : RComponent<CheckerAppProps, AppState>() {
    private fun handleChange(event: Event) {
        val target = event.target as HTMLSelectElement
        setState {
            console.log("value = ${target.value}")
        }
    }

    override fun RBuilder.render() {
        bindToViewModel()
        val viewmodel = props.viewModel

        if (state.board != null) {

            styledDiv {
                css {
                    display = Display.inlineBlock
                    height = 90.vh
                    display = Display.flex
                    flexDirection = FlexDirection.column
                    justifyContent = JustifyContent.center
                    textAlign = TextAlign.center
                    paddingTop = 1.vmin
                    paddingBottom = 1.vmin
                }
                select {
                    attrs.onChangeFunction = {handleChange(it)}


                    option { +"Minimax" ; }
                    option { +"Mcts" }
                    option { +"Alpha-Beta" }
                    option { +"Human";  }

                }

                //header { +"Timer: ${state.timerSec} sec" }
                statusBox("Player Two") { css { top = 1.vmin } }
                div { boardUi(state.board!!, onBoardClick = { viewmodel.boardClicked(it) }) }
                statusBox("Player One") { css { bottom = 1.vmin } }

            }

        }

    }

    private var isBinded = false
    private fun bindToViewModel() {
        if (!isBinded) {
            isBinded = true
            props.viewModel.board.observeForever {
                setState { board = it }
            }
            props.viewModel.timerSec.observeForever {
                console.info("timer = $it-------------")
                setState { timerSec = it }
            }
        }

    }
}

fun RBuilder.checkerApp(handler: CheckerAppProps.() -> Unit): ReactElement {
    return child(CheckerApp::class) {
        this.attrs(handler)
    }
}


inline fun RBuilder.statusBox(text: String = "", crossinline block: StyledDOMBuilder<DIV>.(RBuilder) -> Unit) {
    styledDiv {
        block.invoke(this, this@statusBox)
        //game status
        css {
            display = Display.inlineBlock
            padding = 1.vmin.toString()
            backgroundColor = Color("#fff")
            borderColor = Color("#deb887")
            borderWidth = 0.5.vmin
            borderStyle = BorderStyle.solid
            width = 45.vmin
            fontSize = 4.vmin
            fontFamily = "Comfortaa ,cursive"
            position = Position.relative
            zIndex = 3
            borderRadius = 1.vmin
            textAlign = TextAlign.center
            margin = "${0} ${LinearDimension.auto}"
        }
        +text
    }
}





