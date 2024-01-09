package org.atlscalameetup.tictactoe

import zio.*

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