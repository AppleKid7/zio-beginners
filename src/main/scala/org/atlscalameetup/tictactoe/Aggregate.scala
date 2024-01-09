package org.atlscalameetup.tictactoe

import zio.Task

trait Aggregate[Cmd, Result, State]:
  def acceptCommand(cmd: Cmd): zio.ZIO[Any, Nothing, Result]
  def queryState: Task[State]