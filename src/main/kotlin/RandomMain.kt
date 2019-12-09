import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import model.algorithm.AlphaBetaAlgo
import model.game.Checkers.*

import model.game.GameController
import model.game.BoardGame
import model.player.CheckersAiPlayer
import model.player.CheckersHumanPlayer
import react.dom.*
import viewmodel.CheckersGameViewModel

import kotlin.browser.document
import kotlin.browser.window
fun main() {
    val player1 = CheckersHumanPlayer()
    val player2 = CheckersAiPlayer(AlphaBetaAlgo(5))
    val viewModel = CheckersGameViewModel(player1,player2)

    window.onload = {
        render(document.getElementById("root")) {
            h1 { +"Hello World!3" }
            checkerApp { this.viewModel = viewModel }
        }
        viewModel.startGame()

    }




}


