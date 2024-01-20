package org.atlscalameetup.tictactoe

import zio.*

object Main extends ZIOAppDefault {
  val run = (for {
    controller <- ZIO.environment[Controller]
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
