package org.atlscalameetup.tictactoe

// Pure function GameState => String

object View {
  def render(state: GameState): String = {
    state match
      case GameState.GameOver(winner) => s"Game over! $winner won"
      case GameState.Playing(board, _) =>
        val result = board.board.map { rows =>
          rows.map { col =>
            col.map(_.toString).getOrElse("-")
          }.mkString(" | ")
        }.mkString("\n----------\n")
        s"\n$result\n"
  }
}
