package chekers.model.game

import cartesianFor
import chekers.model.game.board.pieces.HalfKing

class InternationalCheckers(firstPlayer: Player = Player.Player1,boardSize:Int =BOARD_SIZE): CheckersGame(firstPlayer,boardSize) {
    override fun initBoard() {
        currentPlayer = firstPlayer
        board.clear()

        cartesianFor(board.size / 2 - 1, (board.size + 1) / 2) { line, i ->
            board[line, 2 * i + (line + 1) % 2] = HalfKing(Player.Player2)
            board[board.size - 1 - line, 2 * i + (board.size - line) % 2] = HalfKing(Player.Player1)
        }
    }
}