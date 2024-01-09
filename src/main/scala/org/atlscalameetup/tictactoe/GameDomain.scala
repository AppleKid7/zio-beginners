package org.atlscalameetup.tictactoe
import org.atlscalameetup.tictactoe.GameDomain.Square
import org.atlscalameetup.tictactoe.GameDomain.Square.{Empty, Played}
import zio.{Task, UIO, ZIO}

object GameDomain:
  enum Ruleset:
    case XGoesFirst

  enum Player:
    case X
    case O

  enum Square:
    case Played(p: Player)
    case Empty

  import Player._
  import Square._

  // Array is used because these are fixed-size and small
  type BoardRepr = Array[Array[Square]]

  val emptyBoard: BoardRepr = Array(
    Array(Empty, Empty, Empty),
    Array(Empty, Empty, Empty),
    Array(Empty, Empty, Empty)
  )

  extension (board: BoardRepr)
    def isEmpty: Boolean = board.flatten.forall {
      case Empty => true
      case _ => false
    }

    def isFull: Boolean = board.flatten.forall {
      case Played(_) => true
      case Empty => false
    }

    def get(pos:Position): Square = board(pos.row)(pos.col)

    def set(pos:Position, player: Player): BoardRepr =
      board.updated(pos.row, board(pos.row).updated(pos.col, Square.Played(player)))

    def countPlays: Int = board.flatten.count {
      case Played(p) => true
      case Empty => false
    }

    def extractRow(row:Int): Seq[Square] = board(row)

    def extractCol(col:Int): Seq[Square] = board.map(_(col))

    def extractDiag1(): Seq[Square] = Array(board(0)(0), board(1)(1), board(2)(2))

    def extractDiag2(): Seq[Square] = Array(board(0)(2), board(1)(1), board(2)(0))

    def extractTriples(): Seq[Seq[Square]] =
      val indices = 0.to(2)
      indices.map(extractRow) ++ indices.map(extractCol) :+ extractDiag1() :+ extractDiag2()

    def hasWinner(player:Player): Boolean =
      extractTriples().exists { triple =>
        triple.forall {
          case Played(p) if p == player => true
          case _ => false
        }
      }

  def getNextPlayer(rules: Ruleset, board: BoardRepr): Player =
    rules match
      case Ruleset.XGoesFirst => if ((board.countPlays % 2) == 0) X else O

  enum GameState:
    case NotStarted
    case InProgress(rules: Ruleset, boardRepr: BoardRepr)
    case Finished(finalBoard: BoardRepr)

  enum GameCommand:
    case Start(ruleSet: Ruleset)
    case Play(who: Player, pos: Position)

  enum GameResult(val message: String):
    case Success(nextPlayer: Player, boardRepr: BoardRepr) extends GameResult(s"Play accepted.")
    case HasNotStarted extends GameResult(s"The game must be started first")
    case OutOfTurn(nextPlayer: Player) extends GameResult(s"You played out of turn; $nextPlayer is next.")
    case SquareOccupied(nextPlayer: Player) extends GameResult(s"That square is already occupied. $nextPlayer is next.")
    case Won(winner: Player, finalBoard: BoardRepr) extends GameResult(s"The game has ended. $winner has won.")
    case Drew(finalBoard: BoardRepr) extends GameResult(s"The game is a draw.")
    case AlreadyFinished extends GameResult("Game is finished.")
    case AlreadyStarted extends GameResult("Cannot restart an already started game.")

  trait TicTacToeAggregate extends Aggregate[GameCommand, GameResult, GameState]

  def makeGame(): Task[TicTacToeAggregate] =
    import GameState._
    import GameResult._
    import GameCommand._
    for {
      stateRef <- zio.Ref.make[GameState](GameState.NotStarted)
    } yield {
      new TicTacToeAggregate:
        override def acceptCommand(cmd: GameCommand): zio.ZIO[Any, Nothing, GameResult] =
          stateRef.modify {
            case NotStarted =>
              cmd match
                case Start(ruleSet) =>
                  val newBoard = emptyBoard
                  val nextPlayer = getNextPlayer(ruleSet, newBoard)
                  (Success(nextPlayer, newBoard), InProgress(ruleSet, newBoard))
                case _ =>
                  (HasNotStarted, NotStarted)


            case state@InProgress(rules, board) =>
              cmd match
                case _:Start =>
                  (AlreadyStarted, state)
                case Play(player, pos) =>
                  val expectedNextPlayer = getNextPlayer(rules, board)
                  if(expectedNextPlayer != player) then
                    (OutOfTurn(expectedNextPlayer), state)
                  else
                    board.get(pos) match
                      case Square.Played(p) =>
                        (SquareOccupied(expectedNextPlayer), state)
                      case Square.Empty =>
                        val newBoard = board.set(pos, player)
                        if (newBoard.hasWinner(player)) then
                          (Won(player, newBoard), Finished(newBoard))
                        else
                          if (newBoard.isFull) then
                            (Drew(newBoard), Finished(newBoard))
                          (Success(getNextPlayer(rules, newBoard),  newBoard), InProgress(rules, newBoard))

            case state:Finished =>
              (AlreadyFinished, state)
            }

        override def queryState: UIO[GameState] =
          stateRef.get
    }


