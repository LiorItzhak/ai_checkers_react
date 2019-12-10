package model.player

import kotlinx.coroutines.channels.Channel
import model.Board
import model.Piece
import model.game.Checkers.Move
import model.game.BoardGame

abstract class HumanPlayer<G:BoardGame<M,out Board<out Piece>>,M:Move,H :HumanPlayer.HumanMove<M>>(name: String): Player<G,M>(name) {
    private var humanClickChannel: Channel <Pair<Int, Int>>? = null

    override suspend fun calcMove(game: G, backupMove: CommittedMove<M>): M {
        var  hMove :H? = null
        val gameCopy =game.copy()
        while (hMove?.waitForAnotherClick != false){
            console.info("debug: wait for human click")
            //register to click board listener
            val humanClickChannel = Channel< Pair<Int, Int>>(1).also { humanClickChannel = it }
            val click = humanClickChannel.receive()//wait for click event
            console.info("debug: received humans' click ${click.second}")
            hMove = humanMove(gameCopy.copy() as G, click, hMove)
            listeners.forEach { it.onHumanTakeMove(player,gameCopy.copy() as G,hMove) }
        }

        return hMove.move!!//TODO()!!
    }

    abstract suspend fun humanMove(game:G, clickCoordinate: Pair<Int, Int>, previousPart: H?): H

    fun onBoardClicked(coordinate: Pair<Int, Int>) {
        humanClickChannel?.offer( coordinate)
    }

    class HumanMove<M:Move>(val move:M? , val waitForAnotherClick: Boolean = false,val data :Any?=null)

    private val listeners = mutableListOf<IHumanPlayerListener<G,M>>()
    fun addListener(listener: IHumanPlayerListener<G,M>) = listeners.add(listener)
    fun removeListener(listener: IHumanPlayerListener<G,M>) = listeners.remove(listener)

    interface IHumanPlayerListener<G:BoardGame<M,out Board<out Piece>>,M:Move>{
        fun onHumanTakeMove(player: BoardGame.Player,game:G, move : HumanMove<M>)
    }
}