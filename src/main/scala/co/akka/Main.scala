package co.akka

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import co.akka.balance.GeneralBalance
import co.akka.http.BalanceRoute
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext}
import scala.util.{Failure, Success}

object Main extends App with LazyLogging {
  implicit val system: ActorSystem = ActorSystem("general-balance")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val executionContext: ExecutionContext = system.dispatcher
  val proxy: ActorRef = system.actorOf(GeneralBalance.props)

  Http()
    .bindAndHandle(BalanceRoute.route(proxy), "0.0.0.0", 8080)
    .onComplete {
      case Success(Http.ServerBinding(localAddress)) =>
        logger.info(
          s"Http service started - Listening for HTTP on $localAddress")
      case Failure(exception) =>
        logger.error("There was an exception bindings http service", exception)
        Await.ready(system.terminate(), 10.seconds)
    }
}
