package lib

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

import play.api.libs.json.Json
import play.api.libs.ws.WS
import play.api.Play.current

import models.{RecommendationItems, RecommendationResponse, RecommenderRequest}

class RecommendationClient(baseUri: String) {
  def getRecommendations(userId: String): Future[List[RecommendationItems]] = {
    val request = Json.toJson(RecommenderRequest(userId))
    val recommendations = for {
      response <- WS.url(baseUri).post(request) if response.status == 200
    } yield response.json.validate[RecommendationResponse].map(_.itemScores).getOrElse(List.empty)

    recommendations recover { case _ => List.empty }
  }
}
