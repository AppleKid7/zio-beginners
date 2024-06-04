package org.atlscalameetup.tictactoe

import zio.*

object Main extends ZIOAppDefault {
  val run = (for {
    controller <- ZIO.environment[Controller]
    initialBoard = TicTacToeBoard.initial
    initialState = GameState.Playing(initialBoard, Mark.X)
    _ <- controller.get.gameLoop(initialState)
  } yield ()).provide(
    ZLayer.succeed(Console.ConsoleLive),
    LiveController.make
  )
}
