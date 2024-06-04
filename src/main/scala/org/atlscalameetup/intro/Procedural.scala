package org.atlscalameetup.intro


def aTransactionProcedural(amount: Money): Money = {
  val withProcessingFee = (amount * 1.05).setScale(2, BigDecimal.RoundingMode.HALF_UP)
  println(s"${YELLOW}sending amount to db $$$withProcessingFee${RESET}")
  withProcessingFee
}

object Procedural1 {
  def main(args: Array[String]): Unit = {
    val transactions = aTransactionProcedural(100) + aTransactionProcedural(100)
    println(s"${BLUE}transaction: $transactions${RESET}")
  }
}

object Procedural2 {
  def main(args: Array[String]): Unit = {
    val transaction = aTransactionProcedural(100)
    val twoTransactions = transaction + transaction
    println(s"${BLUE}two transactions: $twoTransactions${RESET}")
  }
}

// Referential transparency!

