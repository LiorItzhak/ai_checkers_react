package chekers.model.players

import kotlinx.coroutines.delay
import chekers.model.game.CheckersGame
import chekers.model.game.CheckersMove
import boradGames.players.AiPlayer


class CheckerRandomPlayer(private val delay:Long = 1000, name: String? = null) : AiPlayer<CheckersGame, CheckersMove>(name ?: "Random Player") {
    override suspend fun calcMove(game: CheckersGame, backupMove: CommittedMove<CheckersMove>): CheckersMove {
        backupMove.commit(game.getRandomMove(player))
        delay(delay)
        return game.getRandomMove(player)
    }

}