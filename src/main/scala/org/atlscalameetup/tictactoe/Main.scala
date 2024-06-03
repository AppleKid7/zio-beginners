package org.atlscalameetup.tictactoe

import zio.*

object Main extends ZIOAppDefault {
  val run = (for {
    controller <- ZIO.environment[Controller]
    _ <- controller.get.gameLoop()
  } yield ()).provide(
    ZLayer.succeed(Console.ConsoleLive),
    LiveController.make(GameState.initial),
    // LiveController.make(TicTacToeBoard.initial)
  )
}
