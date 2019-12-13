import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import model.algorithm.AlphaBetaAlgo
import model.game.Checkers.*

import model.game.GameController
import model.game.BoardGame
import model.player.CheckersAiPlayer
import model.player.CheckersHumanPlayer
import model.player.CheckersMctsAiPlayer
import react.dom.*
import viewmodel.CheckersGameViewModel

import kotlin.browser.document
import kotlin.browser.window
fun main() {
    val player2 =  CheckersMctsAiPlayer()//CheckersAiPlayer(AlphaBetaAlgo(10))//CheckersMctsAiPlayer()CheckersHumanPlayer
    val player1 = CheckersAiPlayer(AlphaBetaAlgo(9))
    val viewModel = CheckersGameViewModel(player1,player2,timeLimitMillis = 5000)

    window.onload = {
        render(document.getElementById("root")) {
            h1 { +"Hello World!3" }
            checkerApp { this.viewModel = viewModel }
        }
    }
    viewModel.startGame()
}


