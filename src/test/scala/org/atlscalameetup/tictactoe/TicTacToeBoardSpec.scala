package org.atlscalameetup.tictactoe

import zio.test.*
import zio.test.Assertion.*

object TicTacToeBoardSpec extends ZIOSpecDefault  {
  def spec = suite("TicTacToeBoardSpec")(
    test("No winner") {
      val board = TicTacToeBoard(
        Vector(
          Vector.tabulate[Option[Mark]](3)(_ => None),
          Vector.tabulate[Option[Mark]](3)(_ => None),
          Vector.tabulate[Option[Mark]](3)(_ => None)
        ))
      val result = board.checkWinner
      assertTrue(result == None)
    },
    test("X is winner") {
      val board = TicTacToeBoard(
        Vector(
          Vector(Some(Mark.X), Some(Mark.X), Some(Mark.X)),
          Vector.tabulate[Option[Mark]](3)(_ => None),
          Vector(Some(Mark.O), None, Some(Mark.O))
        ))
      val result = board.checkWinner
      assertTrue(result == Some(Mark.X))
    },
    test("placing X in 0,0") {
      val board: TicTacToeBoard = TicTacToeBoard(
        Vector(
          Vector.tabulate[Option[Mark]](3)(_ => None),
          Vector.tabulate[Option[Mark]](3)(_ => None),
          Vector.tabulate[Option[Mark]](3)(_ => None)
        ))
      val result = board.placeMark(Mark.X, Position(0, 0))
      val expected = Right(TicTacToeBoard(
        Vector(
          Vector(Some(Mark.X), None, None),
          Vector.tabulate[Option[Mark]](3)(_ => None),
          Vector.tabulate[Option[Mark]](3)(_ => None)
        )))
      assertTrue(result == expected)
    },
    test("placing X in 2,1 when there's already an X in 0, 0") {
      val board: TicTacToeBoard = TicTacToeBoard(
        Vector(
          Vector(Some(Mark.X), None, None),
          Vector.tabulate[Option[Mark]](3)(_ => None),
          Vector.tabulate[Option[Mark]](3)(_ => None)
        ))
      val result = board.placeMark(Mark.X, Position(2, 1))
      val expected = Right(TicTacToeBoard(
        Vector(
          Vector(Some(Mark.X), None, None),
          Vector(None, None, Some(Mark.X)),
          Vector.tabulate[Option[Mark]](3)(_ => None),
        )))
      assertTrue(result == expected)
    },
    test("placing O in 2,1 when it's already taken") {
      val board: TicTacToeBoard = TicTacToeBoard(
        Vector(
          Vector(Some(Mark.X), None, None),
          Vector.tabulate[Option[Mark]](3)(_ => None),
          Vector.tabulate[Option[Mark]](3)(_ => None)
        ))
      val result = board.placeMark(Mark.O, Position(0, 0))
      assertTrue(result.isLeft,
        result match {
        case Left(e) => e.isInstanceOf[Throwable]
        case _ => false
      })
    }
  )
}
