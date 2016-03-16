package lib.userhistory.models

import org.joda.time.DateTime
import play.api.libs.json.Json

object Action {
  import models.JodaImplicits._
  implicit val jf = Json.format[Action]
}

case class Action(
  browserId: String,
  userId: Option[String],
  platform: Device,
  url: String,
  section: Option[String],
  timestamp: DateTime,
  pageType: PageType)
