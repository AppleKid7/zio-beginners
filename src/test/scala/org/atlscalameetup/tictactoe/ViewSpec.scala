package org.atlscalameetup.tictactoe

import zio.test.*

object ViewSpec extends ZIOSpecDefault {
  def spec = suite("ViewSpec")(
    test("New game") {
      val board = TicTacToeBoard(
        Vector(
          Vector.tabulate[Option[Mark]](3)(_ => None),
          Vector.tabulate[Option[Mark]](3)(_ => None),
          Vector.tabulate[Option[Mark]](3)(_ => None)
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
    test("New game") {
      val board = TicTacToeBoard(
        Vector(
          Vector.tabulate[Option[Mark]](3)(_ => None),
          Vector.tabulate[Option[Mark]](3)(_ => None),
          Vector.tabulate[Option[Mark]](3)(_ => None)
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
  )
}
