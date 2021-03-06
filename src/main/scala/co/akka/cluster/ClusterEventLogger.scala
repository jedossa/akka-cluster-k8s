package co.akka.cluster

import akka.actor._
import akka.cluster.ClusterEvent._
import akka.cluster.{Cluster, Member}
import com.typesafe.scalalogging.LazyLogging

final class ClusterEventLogger(role: ClusterRole)
    extends Actor
    with LazyLogging {
  private[ClusterEventLogger] val cluster: Cluster = Cluster(context.system)

  override def preStart(): Unit = {
    cluster.subscribe(
      self,
      classOf[MemberUp],
      classOf[MemberExited],
      classOf[MemberRemoved],
      classOf[LeaderChanged],
      classOf[RoleLeaderChanged],
      classOf[UnreachableMember],
      classOf[ReachableMember]
    )

    loadClusterState()
  }

  override def postStop(): Unit = cluster.unsubscribe(self)

  def receive: Receive = {
    case MemberUp(m)             => registerNode(m)
    case MemberExited(m)         => deregisterNode(m)
    case MemberRemoved(m, _)     => deregisterNode(m)
    case LeaderChanged(m)        => inform(m)
    case UnreachableMember(m)    => deregisterNode(m)
    case ReachableMember(m)      => registerNode(m)
    case cs: CurrentClusterState => inform(cs)
  }

  private[ClusterEventLogger] def loadClusterState(): Unit = {
    logger.debug(s"== Node startup ==")
    cluster.state.members.foreach { member =>
      logger.debug(s"member.uniqueAddress = ${member.uniqueAddress}")
      registerNode(member)
    }
  }

  private[ClusterEventLogger] def registerNode(member: Member): Unit = {
    logger.debug(s"== Node up ==")
    logger.debug(s"member.roles = ${member.roles}")
  }

  private[ClusterEventLogger] def deregisterNode(member: Member): Unit = {
    logger.debug(s"== Node down ==")
    logger.debug(s"member.roles = ${member.roles}")
  }

  private[ClusterEventLogger] def inform(leader: Option[Address]): Unit =
    leader foreach { l =>
      logger.info("== LeaderChanged ==")
      logger.info(s"leader.protocol = ${l.protocol}")
      logger.info(s"leader.system = ${l.system}")
      logger.info(s"leader.host = ${l.host}")
      logger.info(s"leader.port = ${l.port}")
    }

  private[ClusterEventLogger] def inform(state: CurrentClusterState): Unit = {
    logger.info("== CurrentClusterState ==")
    logger.info(s"clusterState.leader = ${state.leader}")
    logger.info(s"clusterState.members = ${state.members}")
    logger.info(s"clusterState.roleLeaderMap = ${state.roleLeaderMap}")
    logger.info(s"clusterState.seenBy = ${state.seenBy}")
    logger.info(s"clusterState.unreachable = ${state.unreachable}")
  }

}

object ClusterEventLogger {
  def props(role: ClusterRole): Props = Props(new ClusterEventLogger(role))
}
