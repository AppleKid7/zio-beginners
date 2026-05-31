package org.atlscalameetup.tictactoe

import zio.*
import scala.util.{Success, Try}
import cats.syntax.writer
import org.atlscalameetup.tictactoe.GameState.Playing
import org.atlscalameetup.tictactoe.Mark.X

// ZIO stuff here
// all the I/O goes here
// gameLoop: read some input and write some output and check to see if we're done
// Console.printLine, Console.readLine
// write tests, delete them, then write them again

trait Controller { // Implementation of Controller can depend on Console
  def gameLoop(gameId: String): Task[Unit]
  def parseCommand(input: String): Either[InputError, Command]
}

enum InputError:
  case GeneralConsoleError
  case ParsingError
  case WrongInput(message: String)
  def asString: String =
    this match {
      case WrongInput(msg) => msg
      case _ => this.toString
    }

enum Command:
  case Quit
  case Play(mark: Mark, position: Position)

case class LiveController(console: Console, repo: Repository) extends Controller {
  val freshGame: GameState = Playing(TicTacToeBoard.initial, X)

  override def gameLoop(gameId: String): Task[Unit] =
    for {
      game <- repo.getGame(gameId)
      oldState <- game match {
        case None =>
          console.printLine(s"starting new game with id $gameId") *>
            ZIO.succeed(freshGame)
        case Some(s) =>
          ZIO.succeed(s)
      }
      currentMark <- ZIO.fromOption(oldState.getMark).mapError(_ => new Throwable("getMark error"))
      _ <- console.printLine(s"$currentMark's turn")
      input <- console.readLine(">>> ")
      maybeValidated = parseCommand(s"$currentMark: $input")
      (errorOpt, updatedState) = maybeValidated match {
        case Right(command) =>
          handleCommand(command, oldState.getBoard) match {
            case Right(newState) => (None, newState)
            case Left(error) => (Some(error), oldState)
          }
        case Left(error) => (Some(error), oldState)
      }
      _ <- errorOpt match {
        case Some(error) => console.printLine(error)
        case None => ZIO.unit
      }
      _ <- updatedState match {
        case updatedState: GameState.Playing =>
          repo.putGame(gameId, updatedState) *>
            console.printLine(View.render(updatedState)) <*> gameLoop(gameId)
        case updatedState: GameState.GameOver =>
          repo.putGame(gameId, updatedState) *>
            console.printLine(View.render(updatedState))
      }
    } yield ()

  override def parseCommand(input: String): Either[InputError, Command] = {
    def checkBounds(col: Int, row: Int): Boolean =
      if ((col < 0 || col > 2) || (row < 0 || col > 2)) false
      else true

    input match {
      case s"$mark: $col, $row" if (mark == "X" || mark == "O") =>
        (col.toIntOption, row.toIntOption) match {
          case (Some(col), Some(row)) if checkBounds(col, row) =>
            Right(Command.Play(Mark.valueOf(mark), Position(col, row)))
          case (None, None) => Left(InputError.ParsingError)
          case _ =>
            Left(
              InputError.WrongInput(
                "Please make sure your input is in the correct numerical format: row, column"
              )
            )
        }
      case _ if input.contains("exit") => Right(Command.Quit)
      case _ => Left(InputError.ParsingError)
    }
  }

  private def handleCommand(
      command: Command,
      board: TicTacToeBoard
  ): Either[InputError, GameState] =
    val winner = board.checkWinner
    command match {
      case Command.Quit => Right(GameState.GameOver(board, winner))
      case Command.Play(currentMark, position) =>
        board.placeMark(currentMark, position) match {
          case Right(board) =>
            val winner = board.checkWinner
            if (board.isFull && winner.isEmpty) Right(GameState.GameOver(board, None))
            else if (winner.nonEmpty) Right(GameState.GameOver(board, winner))
            else {
              val newMark = currentMark match {
                case Mark.O => Mark.X
                case _ => Mark.O
              }
              Right(GameState.Playing(board, newMark))
            }
          case Left(GameRulesError.SpaceAlreadyTaken) =>
            Left(InputError.WrongInput("Can't place mark on a non-empty space!"))
        }
    }
}
// make companion object with live method

// FP to the max then watch FP to the min
// 1) in FP instead of doing things we describe them with data structures
//    This lets us refactor without fear and lets us separate how we do something
//    from what we do (retries, doing something in the background, doing something in parallel, etc)
//    in Java you bundle together the how with the what.
// 2) effect to transform that does the first effect but in a retry fashion
//    transform. Use fork to do something in the background, timeout,
//
//# create the app separating business logic
//# delete ZIO parts
//# re write and delete and rewrite at least one cycle like that
//
//representation of board state
//  Xs Os
//  illegal moves?

object LiveController {
  def make: ZLayer[Console & Repository, Nothing, LiveController] =
    ZLayer.fromFunction(LiveController.apply)
//    ZLayer.scoped {
//      for {
//        console <- ZIO.service[Console]
//        repo <- ZIO.service[Repository]
//      } yield LiveController(console, repo)
//    }
}
