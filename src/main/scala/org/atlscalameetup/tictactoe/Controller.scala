package org.atlscalameetup.tictactoe

import org.atlscalameetup.tictactoe.GameDomain.GameResult.Won
import org.atlscalameetup.tictactoe.GameDomain.Player.X
import org.atlscalameetup.tictactoe.GameDomain.Ruleset.XGoesFirst
import org.atlscalameetup.tictactoe.GameDomain.{BoardRepr, GameCommand, GameResult, GameState, Player, Square, TicTacToeAggregate, makeGame}
import zio.*
import zio.Console.*

import scala.util.{Success, Try}

// ZIO stuff here
// all the I/O goes here
// gameLoop: read some input and write some output and check to see if we're done
// Console.printLine, Console.readLine
// write tests, delete them, then write them again

trait Controller { // Implementation of Controller can depend on Console
  def runGame: Task[Unit]
}

case class LiveController() extends Controller {
  override def runGame: Task[Unit] = ??? // use Console
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



object EffectfulMain extends ZIOAppDefault:
  
  private def isInProgress(game: TicTacToeAggregate): Task[Boolean] =
    game.queryState.map {
      case GameState.NotStarted =>       true
      case GameState.InProgress(_, _) => true
      case GameState.Finished(_) =>      false
    }

  private val standardFailure = ParseResult.Failure("Failed to parse command, try 'Start', 'X (row) (col)', 'O (row) (col)'")

  private enum ParseResult:
    case Exit
    case Failure(message: String)
    case Success(command: GameCommand)
  
  private def parse(input:String): ParseResult =
    input.trim.toUpperCase match
      case "QUIT" => ParseResult.Exit
      case "EXIT" => ParseResult.Exit
      case "START" =>
        ParseResult.Success(GameCommand.Start(XGoesFirst))
      case s"$player $row $col" =>
        val playerObj = Try(Player.valueOf(player))
        (playerObj, row.toIntOption, col.toIntOption) match
          case (Success(p), Some(r), Some(c)) if r < 3 && c < 3 => ParseResult.Success(GameCommand.Play(p, Position(c, r)))
          case _ => standardFailure
      case _ => standardFailure


  private def formatRow(row: Array[Square]): String =
    " " + row.map {
      case Square.Played(Player.X) => "X"
      case Square.Played(Player.O) => "O"
      case Square.Empty => " "
    }.mkString(" │ ") + " "

  private def formatBoard(board: BoardRepr): String =
    "\n" + board.map(formatRow).mkString("\n───┼───┼───\n") + "\n"
    
  private def formatResult(result: GameResult): String =
    result match 
      case GameResult.Success(nextPlayer, boardRepr) =>
        s"Success! Player $nextPlayer goes next.\n" + formatBoard(boardRepr)
      case w@Won(winner, finalBoard) =>
        s"${w.message}"
      case other: GameResult =>
        other.message

  private def inputLoop(game:TicTacToeAggregate): Task[Unit] =
      for {
        input <- readLine(">>> ")
        _ <- parse(input) match {
          case ParseResult.Exit =>
            for {
              _ <- printLine("Exiting.")
            } yield ()
          case ParseResult.Failure(errMessage) =>
            for {
              _ <- printLine(errMessage)
              _ <- inputLoop(game)
            } yield ()
          case ParseResult.Success(command) =>
            for {
              result <- game.acceptCommand(command)
              output = formatResult(result)
              _ <- printLine(output)
              shouldContinue <- isInProgress(game)
              _ <- if (shouldContinue) inputLoop(game) else printLine("All Finished")
            } yield ()
        }
      } yield ()


  def run: ZIO[Any & ZIOAppArgs & Scope, Throwable, Unit] =
    for {
      newGame <- makeGame()
      _ <- inputLoop(newGame)
    } yield ()
  
    
  
