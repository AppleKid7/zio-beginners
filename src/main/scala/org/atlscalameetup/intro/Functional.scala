package org.atlscalameetup.intro

import zio.*

// ZIO is an effects system based on the IO Monad
// IO Monad + errors and variable R

// Here we're encapsulating that this has an effect. We're describing an effect and
// separating it from the actual value.
def aTransactionFunctional(amount: BigDecimal): Task[Money] = {
  for {
    withProcessingFee <- ZIO.attempt((amount * 1.05).setScale(2, BigDecimal.RoundingMode.HALF_UP))
    _ <- Console.printLine(s"${YELLOW}sending amount to db $$$withProcessingFee${RESET}")
  } yield withProcessingFee
}

object Functional1 extends ZIOAppDefault {
  val twoTransactions = for {
    first <- aTransactionFunctional(100)
    second <- aTransactionFunctional(100)
  } yield (first + second)

  val run = for {
    result <- twoTransactions
    _ <- Console.printLine(s"${BLUE}two transactions: $result${RESET}")
  } yield ()
}

object Functional2 extends ZIOAppDefault {
  // this val is an object that represents the work
  // to be done. It doesn't actually run at this moment.
  val transaction = aTransactionFunctional(100)

  val twoTransactions = for {
    first <- transaction
    second <- transaction
  } yield (first + second)

  // Here we specify how it's going to be run
  val run = for {
    result <- twoTransactions
    _ <- Console.printLine(s"${BLUE}two transactions: $result${RESET}")
  } yield ()
}
