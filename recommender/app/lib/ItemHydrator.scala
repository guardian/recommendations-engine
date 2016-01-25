package lib

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

import play.api.libs.ws.WS
import play.api.Play.current

import data.RecommenderConfiguration

object ItemHydrator {
  def item(id: String): Future[Option[String]] = {
    val body = for {
      response <- WS.url(s"${RecommenderConfiguration.mobileItems}/$id").get() if response.status == 200
    } yield Some(response.body)

    body recover { case _ => None }
  }
}