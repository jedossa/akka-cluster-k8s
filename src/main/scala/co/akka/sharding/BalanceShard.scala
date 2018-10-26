package co.akka.sharding

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.cluster.sharding._
import co.akka.balance.{BalanceMessage, GeneralBalance}
import com.typesafe.scalalogging.LazyLogging

final class BalanceShard extends Actor with LazyLogging {
  override def receive: Receive = {
    case m: BalanceMessage =>
      ClusterSharding(context.system).shardRegion("Balance") ! m
    case _ => logger.error("Not implemented")
  }
}

object BalanceShard {
  def props: Props = Props(new BalanceShard)

  private[this] def shardIdExtractor: (String) => String =
    s => (math.abs(s.hashCode) % 100).toString

  private[this] def extractEntityId: ShardRegion.ExtractEntityId = {
    case m: BalanceMessage => m.id -> m
  }

  private[this] def extractShardId: ShardRegion.ExtractShardId = {
    case m: BalanceMessage           => shardIdExtractor(m.id)
    case ShardRegion.StartEntity(id) => shardIdExtractor(id)
  }

  def startShard(system: ActorSystem): ActorRef =
    ClusterSharding(system).start(
      typeName = "Balance",
      entityProps = GeneralBalance.props,
      settings = ClusterShardingSettings(system),
      extractEntityId = extractEntityId,
      extractShardId = extractShardId
    )

  def proxy(system: ActorSystem): ActorRef =
    ClusterSharding
      .get(system)
      .startProxy(
        typeName = "Balance",
        role = Some("data"),
        extractEntityId = extractEntityId,
        extractShardId = extractShardId
      )

}
