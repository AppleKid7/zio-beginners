package org.atlscalameetup.intro

import zio.*

// ZIO is an effects system based on the IO Monad
// IO Monad + errors and variable R

object Functional extends ZIOAppDefault {
  // Here we're encapsulating that this has an effect. We're describing an effect and
  // separating it from the actual value.
  def aTransaction(amount: BigDecimal): Task[Money] = {
    for {
      withProcessingFee <- ZIO.attempt((amount * 1.05).setScale(2, BigDecimal.RoundingMode.HALF_UP))
      _ <- Console.printLine(s"sending amount to db $$$withProcessingFee")
    } yield withProcessingFee
  }

  val firstTransaction = aTransaction(100)
  val secondTransaction = for {
    first <- aTransaction(100)
    second <- aTransaction(100)
  } yield (first + second)

  val secondTransaction_v2 = for {
    first <- firstTransaction
    second <- firstTransaction
  } yield (first + second)

  val run = for {
//    firstResult <- firstTransaction
//    secondResult <- secondTransaction
    secondResult_v2 <- secondTransaction_v2
//    _ <- Console.printLine(s"using firstTransaction: $firstResult")
//    _ <- Console.printLine(s"using secondTransaction: $secondResult")
    _ <- Console.printLine(s"using secondTransaction_v2: $secondResult_v2")
  } yield ()
}
