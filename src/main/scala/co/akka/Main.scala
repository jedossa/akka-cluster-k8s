package co.akka

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.Http
import akka.management.AkkaManagement
import akka.management.cluster.bootstrap.ClusterBootstrap
import akka.stream.ActorMaterializer
import co.akka.cluster._
import co.akka.http.BalanceRoute
import co.akka.sharding.BalanceShard
import com.typesafe.config.{Config, ConfigFactory}
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext}
import scala.util.{Failure, Success}

object Main extends App with LazyLogging {
  val config: Config = ConfigFactory.load()
  val role: String = config.getString("node.role")
  logger.debug("Cluster Node role = {}", role)

  implicit val system: ActorSystem = ClusterRoleSelect.select(role)
  AkkaManagement(system).start()
  ClusterBootstrap(system).start()

  if ("client" equalsIgnoreCase role) {
    implicit val materializer: ActorMaterializer = ActorMaterializer()
    implicit val executionContext: ExecutionContext = system.dispatcher
    val proxy: ActorRef = BalanceShard.proxy(system)

    Http()
      .bindAndHandle(BalanceRoute.route(proxy), "0.0.0.0", 8080)
      .onComplete {
        case Success(Http.ServerBinding(localAddress)) =>
          logger.info(
            s"Http service started - Listening for HTTP on $localAddress")
        case Failure(exception) =>
          logger.error("There was an exception bindings http service",
                       exception)
          Await.ready(system.terminate(), 10.seconds)
      }
  }

}

object ClusterRoleSelect extends LazyLogging {
  def select(role: String): ActorSystem = {
    role.toLowerCase match {
      case "client" =>
        val node = ClientNode
        val system = node.actorSystem()
        node.init(Client, system)
        system
      case _ =>
        val node = DataNode
        val system = node.actorSystem()
        node.init(Data, system)
        BalanceShard.startShard(system)
        system
    }
  }
}
