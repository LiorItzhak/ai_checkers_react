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
import model.player.HumanPlayer
import react.dom.*
import viewmodel.CheckersGameViewModel

import kotlin.browser.document
import kotlin.browser.window
fun main() {
//    val player1 = CheckersHumanPlayer()//CheckersAiPlayer(AlphaBetaAlgo(10))//CheckersMctsAiPlayer()
    val player1 = CheckersMctsAiPlayer(maxDepth = 25)
    val player2 = CheckersAiPlayer()//CheckersMctsAiPlayer(maxDepth = 50)//CheckersAiPlayer(/*AlphaBetaAlgo(10)*/)
    val viewModel = CheckersGameViewModel(player1,player2,timeLimitMillisPlayer1 = 5_000,timeLimitMillisPlayer2 = 5_000)

    window.onload = {
        render(document.getElementById("root")) {
            checkerApp { this.viewModel = viewModel }
        }
    }
    viewModel.startGame()
}


