package co.akka.http

import akka.actor.ActorRef
import akka.http.scaladsl.model.StatusCodes.{InternalServerError, OK}
import akka.http.scaladsl.server.{Directives, Route}
import akka.pattern.ask
import akka.util.Timeout
import co.akka.balance.{Credit, Debit}

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

object BalanceRoute extends Directives {
  import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
  import io.circe.generic.auto._

  implicit val _: Timeout = Timeout(5.seconds)

  def route(proxy: ActorRef)(implicit ec: ExecutionContext): Route =
    path("credit") {
      post {
        entity(as[Credit]) { c =>
          complete {
            (proxy ? c).map(OK -> _.toString).recover {
              case ex => InternalServerError -> ex.getMessage
            }
          }
        }
      }
    } ~
      path("debit") {
        post {
          entity(as[Debit]) { d =>
            complete {
              (proxy ? d).map(OK -> _.toString).recover {
                case ex => InternalServerError -> ex.getMessage
              }
            }
          }
        }
      }
}
