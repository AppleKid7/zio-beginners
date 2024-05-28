package org.atlscalameetup.tictactoe

import zio.*

object Main extends ZIOAppDefault {
  val run = (for {
    controller <- ZIO.environment[Controller]
    initialBoard = TicTacToeBoard(
      Vector(
        Vector.tabulate[Mark](3)(_ => Mark.Empty),
         Vector.tabulate[Mark](3)(_ => Mark.Empty),
         Vector.tabulate[Mark](3)(_ => Mark.Empty)
      )
    )
    initialState = GameState.Playing(initialBoard, Mark.X)
    _ <- controller.get.gameLoop(initialState)
  } yield ()).provide(
    ZLayer.succeed(Console.ConsoleLive),
    LiveController.make(
      TicTacToeBoard(
        Vector(
          Vector(Mark.X, Mark.O, Mark.X),
          Vector(Mark.O, Mark.O, Mark.X),
          Vector(Mark.X, Mark.X, Mark.O)
        )),
    )
  )
}
