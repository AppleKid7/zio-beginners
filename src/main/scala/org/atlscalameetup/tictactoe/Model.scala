package org.atlscalameetup.tictactoe

import monocle.syntax.all.*


case class Position(col: Int, row: Int) {
  def get(b: TicTacToeBoard) = b.board(col)(row)
  def set(b: TicTacToeBoard, mark: Mark) = b.board(col)(row)
}

case class TicTacToeBoard(board: Vector[Vector[Option[Mark]]]) {
  // Lens for accessing an element at a specific row and column
//  private def elementAt(row: Int, col: Int): Optional[Vector[Vector[Option[Mark]]], Option[Mark]] =
//    GenLens[Vector[Vector[Option[Mark]]]](board)(_.lift(row).flatMap(_.lift(col)))

  def checkWinner: Option[Mark] = {
    val rows = board
    val columns = board.transpose
    val diagonals = Vector(
      (0 until board.size).map(i => board(i)(i)),
      (0 until board.size).map(i => board(i)(board.size - 1 - i))
    )

    val lines = rows ++ columns ++ diagonals
    lines.flatMap { line =>
      if (line.forall(_ == line.head) && line.head.isDefined) line.head
      else None
    }.headOption
  }

  def placeMark(mark: Mark, position: Position): Either[Throwable, TicTacToeBoard] = {
    board(position.row)(position.col) match {
      case None =>
        Right(
          this.focus(_.board.index(position.row).index(position.col)).replace(Some(mark))
        )
      case Some(_) =>
        Left(new Throwable("Can't place a mark on a space that's already taken!"))
    }
  }
}

enum Mark:
  case X, O

enum GameState:
  case GameOver(winner: Mark)
  case Playing(board: TicTacToeBoard, currentTurn: Mark)