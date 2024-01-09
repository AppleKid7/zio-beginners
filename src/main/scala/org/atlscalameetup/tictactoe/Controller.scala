package org.atlscalameetup.tictactoe

import org.atlscalameetup.tictactoe.GameDomain.Player.X
import org.atlscalameetup.tictactoe.GameDomain.Ruleset.XGoesFirst
import org.atlscalameetup.tictactoe.GameDomain.{BoardRepr, GameCommand, GameResult, GameState, Player, Square, TicTacToeAggregate, makeGame}
import zio.*

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
  
  def inProgress(agg: TicTacToeAggregate): Task[Boolean] = {
    agg.queryState.map {
      case GameState.NotStarted => true
      case GameState.InProgress(rules, boardRepr) => true
      case GameState.Finished(finalBoard) => false
    }
  }

  val standardFailure = Left("Failed to parse command, try 'Start', 'X (row) (col)', 'O (row) (col)'")
  
  def parse(input:String): Either[String, GameCommand] =
    input.toUpperCase match {
      case "START" =>
        Right(GameCommand.Start(XGoesFirst))
      case s"$player $row $col" =>
        val playerObj = Try(Player.valueOf(player))
        (playerObj, row.toIntOption, col.toIntOption) match {
          case (Success(p), Some(r), Some(c)) => Right(GameCommand.Play(p, Position(r, c)))
          case _ => standardFailure
        }
      case _ => standardFailure
    }
    
  def formatBoard(b: BoardRepr): String =
    "\n" + (b.map(_.map {
      case Square.Played(p) => p.toString
      case Square.Empty => " "
    }.mkString(" | ")).mkString("\n-----------\n")) + "\n"
    
  def formatResult(result: GameResult): String =
    result match
      case GameResult.Success(nextPlayer, boardRepr) => 
        s"Success! Player $nextPlayer goes next.\n" + formatBoard(boardRepr) 
      case other: GameResult => other.message

  def inputLoop(agg:TicTacToeAggregate): Task[Unit] =
    for {
      input <- zio.Console.readLine(">>> ")
      maybeCommand = parse(input)
      _ <- maybeCommand match {
                case Left(errMessage) =>
                  zio.Console.printLine(errMessage).flatMap {_ => inputLoop(agg)}
                case Right(command) => 
                  for {
                    result <- agg.acceptCommand(command)
                    output = formatResult(result)
                    _ <- zio.Console.printLine(output)
                    continue <- inProgress(agg)
                    _ <- if(continue) inputLoop(agg) else zio.Console.printLine("All Finished")
                  } yield ()
            }
    } yield ()

  def run: ZIO[Any & ZIOAppArgs & Scope, Throwable, Unit] =
    for {
      newGame <- makeGame()
      _ <- inputLoop(newGame)
    } yield ()
  
    
  
