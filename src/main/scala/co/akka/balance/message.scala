package co.akka.balance

sealed trait BalanceMessage {
  val id: String
  val value: Double
}

final case class Credit(id: String, value: Double) extends BalanceMessage

final case class Debit(id: String, value: Double) extends BalanceMessage
