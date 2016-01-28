package lib

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

import play.api.libs.json.Json
import play.api.libs.ws.WS
import play.api.Play.current

import models._

class RecommendationClient(baseUri: String) {
  def getRecommendations(
    userId: String,
    fields: List[QueryBoost],
    dateRangeFilter: Option[DateRangeFilter],
    num: Option[Int]
  ): Future[List[RecommendationItems]] = {
    val request = Json.toJson(RecommenderRequest(userId, fields, dateRangeFilter, num))
    val recommendations = for {
      response <- WS.url(baseUri).post(request) if response.status == 200
    } yield response.json.validate[RecommendationResponse].map(_.itemScores).getOrElse(List.empty)

    recommendations recover { case _ => List.empty }
  }
}
