package models

import org.joda.time.DateTime
import play.api.libs.json.Json

case class QueryBoost(name: String, values: List[String], bias: Float)
object QueryBoost {
  implicit val jf = Json.format[QueryBoost]
}

case class DateRangeFilter(name: String, before: Option[DateTime] = None, after: Option[DateTime] = None)
object DateRangeFilter {
  implicit val jf = Json.format[DateRangeFilter]
}

case class RecommenderRequest(user: String, fields: List[QueryBoost], dateRange: Option[DateRangeFilter], num: Option[Int])
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