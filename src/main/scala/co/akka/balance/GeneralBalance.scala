package co.akka.balance

import akka.actor.Props
import akka.persistence._
import com.typesafe.scalalogging.LazyLogging

import scala.collection.mutable

final class GeneralBalance extends PersistentActor with LazyLogging {
  var messages: mutable.Queue[BalanceMessage] = mutable.Queue()
  var balance: Double = 0D

  private[this] def snapshot(): Unit = {
    if (lastSequenceNr % 1000 == 0 && lastSequenceNr != 0)
      saveSnapshot(GeneralBalance.State(balance, messages))
  }

  private[this] def handler: BalanceMessage => Unit = {
    case c: Credit =>
      logger.debug(s"It is a credit. ID: ${c.id}")
      messages += c
      balance += c.value
      snapshot()
      logger.debug(s"Current balance: $balance")
      sender() ! balance
    case d: Debit =>
      logger.debug(s"It is a debit. ID: ${d.id}")
      messages += d
      balance -= d.value
      snapshot()
      logger.debug(s"Current balance: $balance")
      sender() ! balance
  }

  override def receiveCommand: Receive = {
    case m: BalanceMessage => persist(m)(handler)
  }

  override def receiveRecover: Receive = {
    case m: BalanceMessage => handler(m)
    case SnapshotOffer(_, GeneralBalance.State(b, m)) =>
      balance = b; messages = m
  }

  override def persistenceId: String = s"GeneralBalance-${self.path.name}"
}

object GeneralBalance {
  def props: Props = Props(new GeneralBalance)
  final case class State(balance: Double,
                         messages: mutable.Queue[BalanceMessage])
}
