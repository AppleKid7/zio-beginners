package org.atlscalameetup.intro

object Procedural {
  def main(args: Array[String]): Unit = {
    def aTransaction(amount: Money): Money = {
      val withProcessingFee = (amount * 1.05).setScale(2, BigDecimal.RoundingMode.HALF_UP)
      println(s"sending amount to db $$$withProcessingFee")
      withProcessingFee
    }
    val firstTransaction = aTransaction(100)
//    val secondTransaction = aTransaction(100) + aTransaction(100)
    val secondTransaction_v2 = firstTransaction + firstTransaction
//    println(s"firstTransaction: $firstTransaction")
//    println(s"secondTransaction: $secondTransaction")
    println(s"secondTransaction_v2: $secondTransaction_v2")
  }
}
