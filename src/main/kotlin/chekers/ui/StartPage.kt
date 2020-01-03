package chekers.ui

import chekers.model.players.CheckersHumanPlayer
import kotlinx.html.InputType
import kotlinx.html.id
import kotlinx.html.js.onClickFunction
import chekers.model.game.CheckersGame
import chekers.model.game.CheckersMove
import chekers.model.game.InternationalCheckers
import boradGames.players.*
import chekers.viewmodel.CheckersGameViewModel
import chekers.model.players.CheckersAiPlayer
import chekers.model.players.CheckersMctsAiPlayer
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.HTMLSelectElement
import react.*
import react.dom.*
import kotlin.browser.document
import kotlin.browser.window

const val DEFAULT_BOARD_SIZE = "8"
const val DEFAULT_TIMEOUT = "5000"
const val GAME_TYPE_ID = "GAME_TYPE"
const val BOARD_SIZE_ID = "BOARD_SIZE"
const val TIME_ID = "TIME"
const val PLAYER1_TYPE_ID = "chekers.PLAYER1_TYPE_ID"
const val PLAYER2_TYPE_ID = "chekers.PLAYER2_TYPE_ID"

class StartPage : RComponent<StartPageProps, StartPageState>() {
    override fun RBuilder.render() {
        gameTypeSelect(GAME_TYPE_ID)
        input { attrs.defaultValue = DEFAULT_BOARD_SIZE;attrs.id = BOARD_SIZE_ID;attrs.type = InputType.number }
        playerTypeSelect(PLAYER1_TYPE_ID)
        playerTypeSelect(PLAYER2_TYPE_ID)
        input { attrs.defaultValue = DEFAULT_TIMEOUT;attrs.id = TIME_ID;attrs.type = InputType.number }

        button {
            +"Start"
            attrs.onClickFunction = {
                console.log("start button clicked ")
                val gameSelect = document.getElementById(GAME_TYPE_ID) as HTMLSelectElement
                val target1 = document.getElementById(PLAYER1_TYPE_ID) as HTMLSelectElement
                val target2 = document.getElementById(PLAYER2_TYPE_ID) as HTMLSelectElement
                val time = (document.getElementById(TIME_ID) as HTMLInputElement).value.toLongOrNull()
                val boardSize = (document.getElementById(BOARD_SIZE_ID) as HTMLInputElement).value.toIntOrNull()
                if (time == null || time <= 0) {
                    window.alert("must enter positive integer")
                } else if (boardSize == null || boardSize < 4) {
                    window.alert("board size mus be integer greater or equal to 4")
                } else {
                    val player1 = getPlayer(target1.value)
                    val player2 = getPlayer(target2.value)
                    val game = getGame(gameSelect.value, boardSize)

                    val vm = if (state.viewModel == null)
                        CheckersGameViewModel(
                                game, player1, player2,
                                timeLimitMillisPlayer1 = if (player1 is HumanPlayer<*, *, *>) null else time,
                                timeLimitMillisPlayer2 = if (player2 is HumanPlayer<*, *, *>) null else time)
                    else state.viewModel!!.apply {
                        resetGame(game, player1, player2,
                                timeLimitMillisPlayer1 = if (player1 is HumanPlayer<*, *, *>) null else time,
                                timeLimitMillisPlayer2 = if (player2 is HumanPlayer<*, *, *>) null else time)
                    }

                    setState { state.viewModel = vm }
                    vm.startGame()
                }
            }

        }
        val vm = state.viewModel
        if (vm != null) {
            checkerApp { this.viewModel = vm }
        }
    }
}
interface StartPageProps : RProps
interface StartPageState : RState {
    var viewModel: CheckersGameViewModel?
}

fun RBuilder.playerTypeSelect(id: String) = select {
    attrs.id = id
    option { +"Alpha-Beta" }
    option { +"Mcts" }
    option { +"Mcts Limited Depth(25)" }
    option { +"Human" }
}

fun RBuilder.gameTypeSelect(id: String) = select {
    attrs.id = id
    option { +"International Checkers" }
    option { +"Russian Checkers" }
}

fun getPlayer(str: String): Player<CheckersGame, CheckersMove> = when (str) {
    "Alpha-Beta" -> CheckersAiPlayer()
    "Mcts" -> CheckersMctsAiPlayer(maxDepth = null)
    "Mcts Limited Depth(25)" -> CheckersMctsAiPlayer(maxDepth = 25)
    "Human" -> CheckersHumanPlayer()
    else -> throw UnsupportedOperationException("player of type $str")
}

fun getGame(str: String, boardSize: Int): CheckersGame = when (str) {
    "International Checkers" -> InternationalCheckers(boardSize = boardSize)
    "Russian Checkers" -> CheckersGame(boardSize = boardSize)
    else -> throw UnsupportedOperationException("game of type $str")
}


fun RBuilder.startPage(handler: StartPageProps.() -> Unit): ReactElement {
    return child(StartPage::class) {
        this.attrs(handler)
    }
}