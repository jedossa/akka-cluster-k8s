package co.akka.cluster

import akka.actor.ActorSystem
import com.typesafe.scalalogging.LazyLogging

sealed trait ClusterRole
object Data extends ClusterRole
object Client extends ClusterRole

sealed trait ClusterNode extends LazyLogging {
  def actorSystem(): ActorSystem

  def init(role: ClusterRole, system: ActorSystem): Unit = {
    val clusterEventLogger =
      system.actorOf(ClusterEventLogger.props(role), "clusterEventLogger")
    logger.info(s"clusterEventLogger = ${clusterEventLogger.path}")
  }
}

object ClientNode extends ClusterNode {
  override def init(role: ClusterRole, system: ActorSystem): Unit = {
    super.init(role, system)
  }

  override def actorSystem(): ActorSystem = {
    ActorSystem("general-balance")
  }
}

object DataNode extends ClusterNode {
  override def init(role: ClusterRole, system: ActorSystem): Unit = {
    super.init(role, system)
  }

  override def actorSystem(): ActorSystem = {
    ActorSystem("general-balance")
  }
}
