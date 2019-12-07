//package model.game
//
//import model.game.Checkers.Move
//import ui.BoardView
//import ui.SquareView
//import kotlin.random.Random
//
//class MockGame : IBoardGame<Move>{
//    val size = 8
//    val board : BoardView = BoardView(size, generateRandomBoard())
//
//    override fun initBoard() {
//      board.setSquares(generateRandomBoard())
//    }
//
//    override fun applyMove(move: Move, multiMoveDelayMillis: Long?) {
//        board.setSquares(generateRandomBoard())
//    }
//
//    override fun getRandomMove(player: IBoardGame.Player): Move = MockMove()
//
//    override fun getAllPossibleMoves(player: IBoardGame.Player): List<Move> = listOf()
//
//    override fun isGameEnded(playerTurn: IBoardGame.Player): Boolean = false
//
//    override fun getScore(player: IBoardGame.Player): Int = Random(0).nextInt()
//
//    private fun generateRandomBoard() :List<List<SquareView>> {
//        val highlightList = mutableListOf<SquareView.Color?>(null).apply { addAll(SquareView.Color.values())}
//        val imageList = mutableListOf<SquareView.Image?>(null) .apply { addAll(SquareView.Image.values()) }
//        val onClickList = mutableListOf<((Pair<Int,Int>) -> Unit)?>(null).apply {
//            val x: ((Pair<Int, Int>) -> Unit) = {}
//            add(x)
//        }
//        return (0 until size).map { row->
//            (0 until size).map { col->
//                SquareView(Pair(row,col),highlightList.random(),imageList.random())
//            }.toList()
//        }.toList()
//    }
//    class MockMove : Move()
//}