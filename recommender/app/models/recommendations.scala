package models

import org.joda.time.DateTime
import play.api.libs.json.Json

case class QueryBoost(name: String, values: List[String], bias: Float)
object QueryBoost {
  implicit val jf = Json.format[QueryBoost]
}

case class DateRangeFilter(name: String, before: Option[DateTime] = None, after: Option[DateTime] = None)
object DateRangeFilter {
  import models.JodaImplicits._
  implicit val jf = Json.format[DateRangeFilter]
}

case class RecommendationItems(item: String, score: Double)
object RecommendationItems {
  implicit val jf = Json.format[RecommendationItems]
}

case class ApiRequest(
  webPublicationDate: Option[DateRangeFilter],
  disableDateFilter: Option[Boolean],
  pageSize: Option[Int],
  articles: List[String]
)
object ApiRequest {
  implicit val jf = Json.format[ApiRequest]
}