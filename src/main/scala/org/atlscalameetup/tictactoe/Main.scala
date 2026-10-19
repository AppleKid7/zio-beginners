package org.atlscalameetup.tictactoe

import zio.*

object Main extends ZIOAppDefault {
  val run = (for {
    controller <- ZIO.environment[Controller]
    random <- ZIO.environment[Random]
    gameId <- random.get.nextUUID
    _ <- controller.get.gameLoop(gameId.toString)
  } yield ()).provide(
    ZLayer.succeed(Random.RandomLive),
    ZLayer.succeed(Console.ConsoleLive),
    ZLayer.fromZIO(makeInMemoryRepository),
    LiveController.make
  )
}
