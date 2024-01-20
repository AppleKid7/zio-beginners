package org.atlscalameetup.tictactoe

import monocle.syntax.all.*

enum GameRulesError:
  case SpaceAlreadyTaken

case class Position(col: Int, row: Int) {
  def get(b: TicTacToeBoard) = b.board(col)(row)
  def set(b: TicTacToeBoard, mark: Mark) = b.placeMark(mark, this)
}

case class TicTacToeBoard(board: Vector[Vector[Mark]]) {
  def isEmpty: Boolean = board.flatten.forall {
    case Mark.Empty => true
    case _ => false
  }

  def isFull: Boolean = board.flatten.forall {
    case Mark.Empty => false
    case _ => true
  }

  def checkWinner: Option[Mark] = {
    val rows = board
    val columns = board.transpose
    val diagonals = Vector(
      (0 until board.size).map(i => board(i)(i)),
      (0 until board.size).map(i => board(i)(board.size - 1 - i))
    )

    val lines = rows ++ columns ++ diagonals
    lines.flatMap { line =>
      if (line.forall(_ == line.head) && line.head != Mark.Empty) line
      else None
    }.headOption
  }

  def placeMark(mark: Mark, position: Position): Either[GameRulesError, TicTacToeBoard] = {
    board(position.row)(position.col) match {
      case Mark.Empty =>
        Right(
          this.focus(_.board.index(position.row).index(position.col)).replace(mark)
        )
      case _ =>
        Left(GameRulesError.SpaceAlreadyTaken) //("Can't place a mark on a space that's already taken!")
    }
  }
}

enum Mark:
  case X, O, Empty

enum GameState:
  case GameOver(winner: Mark)
  case Playing(board: TicTacToeBoard, currentTurn: Mark)