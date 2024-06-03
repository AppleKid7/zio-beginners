package org.atlscalameetup.tictactoe

import org.atlscalameetup.tictactoe.GameDomain.Player.X
import org.atlscalameetup.tictactoe.GameDomain.Ruleset.XGoesFirst
import zio.*

import scala.util.{Success, Try}
import cats.syntax.writer

// ZIO stuff here
// all the I/O goes here
// gameLoop: read some input and write some output and check to see if we're done
// Console.printLine, Console.readLine
// write tests, delete them, then write them again

trait Controller { // Implementation of Controller can depend on Console
  def gameLoop(): Task[Unit]
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

case class LiveController(state: Ref[GameState], console: Console) extends Controller {
  override def gameLoop(): Task[Unit] =
    for {
      oldState <- state.get
      currentMark <- ZIO.fromOption(oldState.getMark).mapError(_ => new Throwable("getMark error"))
      _ <- console.printLine(s"$currentMark's turn")
      input <- console.readLine(">>> ")
      maybeValidated = parseCommand(s"$currentMark: $input")
      (errorOpt, updatedState) = maybeValidated match {
        case Right(command) => handleCommand(command, oldState.getBoard) match {
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
        case playing: GameState.Playing =>
          state.set(playing) <*> console.printLine(View.render(playing)) <*> gameLoop()
        case gameOver: GameState.GameOver =>
          state.set(gameOver) <*> console.printLine(View.render(gameOver))
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
          case _ => Left(InputError.WrongInput("Please make sure your input is in the correct numerical format: row, column"))
        }
      case _ if input.contains("exit") => Right(Command.Quit)
      case _ => Left(InputError.ParsingError)
    }
  }

  private def handleCommand(command: Command, board: TicTacToeBoard): Either[InputError, GameState]=
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
          case Left(GameRulesError.SpaceAlreadyTaken) => Left(InputError.WrongInput("Can't place mark on a non-empty space!"))
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
  def make(initalState: GameState): ZLayer[Console, Nothing, LiveController] = ZLayer.scoped {
    for {
      console <- ZIO.service[Console]
      stateRef <- Ref.make[GameState](initalState)
    } yield LiveController(stateRef, console)
  }
}

/*
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
  */
    
  
