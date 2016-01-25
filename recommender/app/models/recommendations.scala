package models

import play.api.libs.json.Json

case class RecommenderRequest(user: String)
object RecommenderRequest {
  implicit val jf = Json.format[RecommenderRequest]
}

case class RecommendationItems(item: String, score: Double)
object RecommendationItems {
  implicit val jf = Json.format[RecommendationItems]
}

case class RecommendationResponse(itemScores: List[RecommendationItems])
object RecommendationResponse {
  implicit val jf = Json.format[RecommendationResponse]
}