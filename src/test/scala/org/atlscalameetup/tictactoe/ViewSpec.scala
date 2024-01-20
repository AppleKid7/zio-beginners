package org.atlscalameetup.tictactoe

import zio.test.*

object ViewSpec extends ZIOSpecDefault {
  def spec = suite("ViewSpec")(
    test("New game") {
      val board = TicTacToeBoard(
        Vector(
          Vector.tabulate[Mark](3)(_ => Mark.Empty),
          Vector.tabulate[Mark](3)(_ => Mark.Empty),
          Vector.tabulate[Mark](3)(_ => Mark.Empty)
        ))
      val result = View.render(GameState.Playing(board, Mark.X))
      val expected =
        """
        |- | - | -
        |----------
        |- | - | -
        |----------
        |- | - | -
        |""".stripMargin
      assertTrue(result == expected)
    },
    test("Game over") {
      val result = View.render(GameState.GameOver(Mark.X))
      val expected = "Game over! X won"
      assertTrue(result == expected)
    },
    test("Game in progress with X at 0,0 and O at 1,1") {
      val board = TicTacToeBoard(
        Vector(
          Vector.tabulate[Mark](3)(_ => Mark.Empty),
          Vector.tabulate[Mark](3)(_ => Mark.Empty),
          Vector.tabulate[Mark](3)(_ => Mark.Empty)
        ))
      for {
        boardX <- board.placeMark(Mark.X, Position(0,0))
        boardY <- boardX.placeMark(Mark.O, Position(1, 1))
        result = View.render(GameState.Playing(boardY, Mark.X))
      } yield {
        val expected =
          """
            |X | - | -
            |----------
            |- | O | -
            |----------
            |- | - | -
            |""".stripMargin
        assertTrue(result == expected)
      }
    },
  )
}
