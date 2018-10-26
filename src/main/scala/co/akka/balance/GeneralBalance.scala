package co.akka.balance

import akka.actor.{Actor, Props}
import com.typesafe.scalalogging.LazyLogging

import scala.collection.mutable

final class GeneralBalance extends Actor with LazyLogging {
  var messages: mutable.Queue[BalanceMessage] = mutable.Queue()
  var balance: Double = 0D

  private[this] def handler: BalanceMessage => Unit = {
    case c: Credit =>
      logger.debug(s"It is a credit. ID: ${c.id}")
      messages += c
      balance += c.value
      logger.debug(s"Current balance: $balance")
      sender() ! balance
    case d: Debit =>
      logger.debug(s"It is a debit. ID: ${d.id}")
      messages += d
      balance -= d.value
      logger.debug(s"Current balance: $balance")
      sender() ! balance
  }

  override def receive: Receive = {
    case m: BalanceMessage => handler(m)
  }
}

object GeneralBalance {
  def props: Props = Props(new GeneralBalance)
  final case class State(balance: Double,
                         messages: mutable.Queue[BalanceMessage])
}
