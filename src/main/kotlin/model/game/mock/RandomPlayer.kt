//package model.player
//
//import kotlinx.coroutines.delay
//import model.game.Checkers.Move
//import model.game.IBoardGame
//
//class RandomPlayer<T : Move>(name: String) : Player<T>(name) {
//    override suspend fun calcMove(game: IBoardGame<T>, backupMove: CommittedMove<T>): T {
//        delay(2000)
//        return game.getRandomMove(player)
//    }
//}
