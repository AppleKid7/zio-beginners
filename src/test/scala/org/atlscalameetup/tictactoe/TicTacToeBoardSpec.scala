package org.atlscalameetup.tictactoe

import zio.test.*
import zio.test.Assertion.*

object TicTacToeBoardSpec extends ZIOSpecDefault  {
  def spec = suite("TicTacToeBoardSpec")(
    test("No winner") {
      val board = TicTacToeBoard(
        Vector(
          Vector.tabulate[Mark](3)(_ => Mark.Empty),
          Vector.tabulate[Mark](3)(_ => Mark.Empty),
          Vector.tabulate[Mark](3)(_ => Mark.Empty)
        ))
      val result = board.checkWinner
      assertTrue(result == None)
    },
    test("X is winner") {
      val board = TicTacToeBoard(
        Vector(
          Vector(Mark.X, Mark.X, Mark.X),
          Vector.tabulate[Mark](3)(_ => Mark.Empty),
          Vector(Mark.O, Mark.Empty, Mark.O)
        ))
      val result = board.checkWinner
      assertTrue(result == Some(Mark.X))
    },
    test("placing X in 0,0") {
      val board: TicTacToeBoard = TicTacToeBoard(
        Vector(
          Vector.tabulate[Mark](3)(_ => Mark.Empty),
          Vector.tabulate[Mark](3)(_ => Mark.Empty),
          Vector.tabulate[Mark](3)(_ => Mark.Empty)
        ))
      val result = board.placeMark(Mark.X, Position(0, 0))
      val expected = Right(TicTacToeBoard(
        Vector(
          Vector(Mark.X, Mark.Empty, Mark.Empty),
          Vector.tabulate[Mark](3)(_ => Mark.Empty),
          Vector.tabulate[Mark](3)(_ => Mark.Empty)
        )))
      assertTrue(result == expected)
    },
    test("placing X in 2,1 when there's already an X in 0, 0") {
      val board: TicTacToeBoard = TicTacToeBoard(
        Vector(
          Vector(Mark.X, Mark.Empty, Mark.Empty),
          Vector.tabulate[Mark](3)(_ => Mark.Empty),
          Vector.tabulate[Mark](3)(_ => Mark.Empty)
        ))
      val result = board.placeMark(Mark.X, Position(2, 1))
      val expected = Right(TicTacToeBoard(
        Vector(
          Vector(Mark.X, Mark.Empty, Mark.Empty),
          Vector(Mark.Empty, Mark.Empty, Mark.X),
          Vector.tabulate[Mark](3)(_ => Mark.Empty),
        )))
      assertTrue(result == expected)
    },
    test("placing O in 2,1 when it's already taken") {
      val board: TicTacToeBoard = TicTacToeBoard(
        Vector(
          Vector(Mark.X, Mark.Empty, Mark.Empty),
          Vector.tabulate[Mark](3)(_ => Mark.Empty),
          Vector.tabulate[Mark](3)(_ => Mark.Empty)
        ))
      val result = board.placeMark(Mark.O, Position(0, 0))
      assertTrue(result.isLeft,
        result match {
        case Left(e) => e.isInstanceOf[Throwable]
        case _ => false
      })
    },
    test("Newly initialized board is empty") {
      val board: TicTacToeBoard = TicTacToeBoard(
        Vector(
          Vector.tabulate[Mark](3)(_ => Mark.Empty),
          Vector.tabulate[Mark](3)(_ => Mark.Empty),
          Vector.tabulate[Mark](3)(_ => Mark.Empty)
        ))
      val result = board.isEmpty
      val expected = true
      assertTrue(result == expected)
    },
    test("Board with all spaces taken is full") {
      val board: TicTacToeBoard = TicTacToeBoard(
        Vector(
          Vector(Mark.X, Mark.O, Mark.X),
          Vector(Mark.O, Mark.O, Mark.X),
          Vector(Mark.X, Mark.X, Mark.O)
        ))
      val result = board.isFull
      val expected = true
      assertTrue(result == expected)
    }
  )
}
