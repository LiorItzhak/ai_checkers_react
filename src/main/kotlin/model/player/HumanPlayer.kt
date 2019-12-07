package model.player

import kotlinx.coroutines.channels.Channel
import model.Board
import model.Piece
import model.game.Checkers.Move
import model.game.BoardGame
import ui.BoardView
import ui.View

abstract class HumanPlayer<G:BoardGame<M,out Board<out Piece>>,M:Move,H :HumanPlayer.HumanMove<M>>(name: String): Player<G,M>(name), BoardView.IOnBoardClickedListener {
    private var humanClickChannel: Channel <Pair<Int, Int>>? = null

    override suspend fun calcMove(game: G, backupMove: CommittedMove<M>): M {
        var  hMove :H? = null
        while (hMove?.waitForAnotherClick != false){
            console.info("debug: wait for human click")
            //register to click board listener
            val humanClickChannel = Channel< Pair<Int, Int>>(1).also { humanClickChannel = it }
            val click = humanClickChannel.receive()//wait for click event
            console.info("debug: received humans' click ${click.second}")
            hMove = humanMove(game, click, hMove)
        }
        //TODO notify board
        return hMove.move!!//TODO()!!
    }

    abstract suspend fun humanMove(game:G, clickCoordinate: Pair<Int, Int>, previousPart: H?): H

    final override fun onBoardClicked(view: View, coordinate: Pair<Int, Int>) {
        humanClickChannel?.offer( coordinate)
    }

    class HumanMove<M:Move>(val move:M? , val waitForAnotherClick: Boolean = false,val data :Any?=null)
}