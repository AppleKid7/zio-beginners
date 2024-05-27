package org.atlscalameetup.tictactoe

// Pure function GameState => String

object View {
  def render(state: GameState): String = {
    def boardToString(board: TicTacToeBoard): String =
      board.board.map { rows =>
        rows.map { col =>
          col match {
            case Mark.Empty => "-"
            case _ => col.toString
          }
        }.mkString(" | ")
      }.mkString("\n----------\n")
    state match
      case GameState.GameOver(board, winner) =>
        val result: String = winner match {
          case Some(winner) => s"${winner.asString} won"
          case None => s"It was a tie!"
        }
        s"""
        |${boardToString(board)}
        |++++++++++++++++++++++
        |Game over! $result""".stripMargin
      case GameState.Playing(board, _) =>
        s"\n${boardToString(board)}\n"
  }
}
